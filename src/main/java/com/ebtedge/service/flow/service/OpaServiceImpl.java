package com.ebtedge.service.flow.service;

import com.ebtedge.service.flow.cardsummary.CardService;
import com.ebtedge.service.flow.cardsummary.CardholderSummaryContext;
import com.ebtedge.service.flow.cardsummary.CardholderSummaryResult;
import com.ebtedge.service.flow.caseinquiry.CaseInquiryContext;
import com.ebtedge.service.flow.caseinquiry.CaseInquiryResult;
import com.ebtedge.service.flow.caseinquiry.CaseService;
import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.core.WorkflowPipelineFactory;
import com.ebtedge.service.flow.domain.*;
import com.ebtedge.service.flow.domain.common.BaseResponseMetadata;
import com.ebtedge.service.flow.domain.common.CardInfoResult;
import com.ebtedge.service.flow.domain.common.ClientInfoResult;
import com.ebtedge.service.flow.domain.ErrorDetails;
import com.ebtedge.service.flow.opa.OpaService;
import com.ebtedge.service.flow.util.CardPrefixUtil;
import com.ebtedge.service.flow.util.CardStatusMapper;
import com.ebtedge.service.flow.util.CardholderIdEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of OpaService using WorkflowPipeline for orchestrating cardholder search.
 * This service demonstrates a production-ready pipeline pattern for complex workflows.
 *
 * Workflow Steps:
 * 1. Fetch cardholder summary (CardService)
 * 2. Fetch case inquiry details (CaseService)
 * 3. Map and merge results into Cardholder domain objects
 * 4. Build CardholderSearchResult
 *
 * This design allows for easy extension with additional steps and automatic observability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpaServiceImpl implements OpaService {

    private final CardService cardService;
    private final CaseService caseService;
    private final WorkflowPipelineFactory pipelineFactory;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public ResponseWrapper<CardholderSearchResult> cardholderSearch(CardholderSearchCriteria searchCriteria) {

        try {
            // Validate input
            validateSearchCriteria(searchCriteria);

            log.info("Starting cardholder search for cardNumber: {}, agency: {}",
                    maskCardNumber(searchCriteria.cardNumber()),
                    searchCriteria.agency());

            // Execute workflow pipeline
            CardholderSearchResult result = pipelineFactory.startWith(searchCriteria)
                    .nextStep("FetchCardholderSummary", this::fetchCardholderSummary)
                    .peek(ctx -> log.debug("Cardholder summary fetched: clientId={}",
                            ctx.summaryResult().clientId()))
                    .nextStep("FetchCaseInquiry", this::fetchCaseInquiry)
                    .peek(ctx -> log.debug("Case inquiry fetched: caseNumber={}, clients={}",
                            ctx.caseResult().caseNumber(), ctx.caseResult().clients().size()))
                    .nextStep("MergeAndBuildCardholders", this::mergeAndBuildCardholders)
                    .peek(ctx -> log.debug("Built {} cardholder(s)", ctx.cardholders().size()))
                    .nextStep("BuildSearchResult", this::buildSearchResult)
                    .mapToUI(result1 -> result1);

            log.info("Successfully completed cardholder search for cardNumber: {}, found {} cardholder(s)",
                    maskCardNumber(searchCriteria.cardNumber()),
                    result.cardholderList().size());

            return ResponseWrapper.success(result);

        } catch (IllegalArgumentException e) {
            log.error("Validation error in cardholderSearch: {}", e.getMessage());
            return ResponseWrapper.fail(new ErrorDetails("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error in cardholderSearch", e);
            return ResponseWrapper.fail(new ErrorDetails("SERVICE_ERROR", "Failed to complete cardholder search"));
        }
    }

    /**
     * Validates the search criteria.
     */
    private void validateSearchCriteria(CardholderSearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }
        if (criteria.cardNumber() == null || criteria.cardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (criteria.agency() == null || criteria.agency().trim().isEmpty()) {
            throw new IllegalArgumentException("Agency is required");
        }
        log.debug("Search criteria validation passed");
    }

    /**
     * Step 1: Fetch cardholder summary from CardService.
     */
    private ResponseWrapper<WorkflowContext> fetchCardholderSummary(CardholderSearchCriteria searchCriteria) {
        log.info("Executing step 1: Fetching cardholder summary");

        CardholderSummaryContext context = new CardholderSummaryContext(
                searchCriteria.baseRequest(),
                searchCriteria.cardNumber(),
                searchCriteria.agency()
        );

        ResponseWrapper<CardholderSummaryResult> summaryResponse = cardService.getCardholderSummary(context);

        return summaryResponse.map(summaryResult -> {
            WorkflowContext workflowContext = new WorkflowContext(
                    searchCriteria,
                    summaryResult,
                    null,
                    null
            );
            log.debug("Cardholder summary fetched successfully for clientId: {}", summaryResult.clientId());
            return workflowContext;
        });
    }

    /**
     * Step 2: Fetch case inquiry from CaseService.
     */
    private ResponseWrapper<WorkflowContext> fetchCaseInquiry(WorkflowContext context) {
        log.info("Executing step 2: Fetching case inquiry");

        CardholderSummaryResult summaryResult = context.summaryResult();

        // Extract primary client to get case number (or generate one for lookup)
        String caseNumber = extractOrGenerateCaseNumber(summaryResult);

        CaseInquiryContext caseContext = new CaseInquiryContext(
                context.searchCriteria().baseRequest(),
                caseNumber,
                context.searchCriteria().agency()
        );

        ResponseWrapper<CaseInquiryResult> caseResponse = caseService.caseInquiry(caseContext);

        return caseResponse.map(caseResult -> {
            WorkflowContext updatedContext = new WorkflowContext(
                    context.searchCriteria(),
                    context.summaryResult(),
                    caseResult,
                    null
            );
            log.debug("Case inquiry fetched successfully for caseNumber: {}", caseResult.caseNumber());
            return updatedContext;
        });
    }

    /**
     * Step 3: Merge CardholderSummaryResult and CaseInquiryResult into Cardholder domain objects.
     */
    private ResponseWrapper<WorkflowContext> mergeAndBuildCardholders(WorkflowContext context) {
        log.info("Executing step 3: Merging results and building cardholders");

        CardholderSummaryResult summaryResult = context.summaryResult();
        CaseInquiryResult caseResult = context.caseResult();

        List<Cardholder> cardholders = new ArrayList<>();

        // Iterate through cards and build Cardholder objects
        for (CardInfoResult cardInfoResult : summaryResult.cards()) {
            // Map CardInfoResult to CardInfo
            CardInfo cardInfo = mapToCardInfo(cardInfoResult);

            // Find matching client from case inquiry
            ClientInfoResult clientInfoResult = findMatchingClient(
                    summaryResult.clientId(),
                    summaryResult.clients()
            );

            if (clientInfoResult != null) {
                // Map ClientInfoResult to ClientInfo with case number from case inquiry
                ClientInfo clientInfo = mapToClientInfo(clientInfoResult, caseResult.caseNumber());

                // Encrypt cardholderId
                String cardholderId = CardholderIdEncryptionUtil.encrypt(
                        context.searchCriteria().agency(),
                        clientInfo.clientId()
                );

                // Calculate score (mock implementation)
                String score = calculateScore(cardInfo, clientInfo);

                // Build Cardholder
                Cardholder cardholder = new Cardholder(cardholderId, score, clientInfo, cardInfo);
                cardholders.add(cardholder);

                log.debug("Built cardholder: cardholderId={}, clientId={}, cardNumber={}",
                        cardholderId, clientInfo.clientId(), maskCardNumber(cardInfo.cardNumber()));
            }
        }

        WorkflowContext updatedContext = new WorkflowContext(
                context.searchCriteria(),
                context.summaryResult(),
                context.caseResult(),
                cardholders
        );

        log.info("Successfully built {} cardholder(s)", cardholders.size());
        return ResponseWrapper.success(updatedContext);
    }

    /**
     * Step 4: Build the final CardholderSearchResult.
     */
    private ResponseWrapper<CardholderSearchResult> buildSearchResult(WorkflowContext context) {
        log.info("Executing step 4: Building search result");

        String requestUuid = context.searchCriteria().baseRequest() != null ?
                context.searchCriteria().baseRequest().uuid() : UUID.randomUUID().toString();
        String currentTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        BaseResponseMetadata baseResponse = new BaseResponseMetadata(requestUuid, currentTimestamp);

        CardholderSearchResult result = new CardholderSearchResult(
                baseResponse,
                context.cardholders(),
                "0" // offset for pagination (future enhancement)
        );

        log.debug("Search result built with {} cardholder(s)", result.cardholderList().size());
        return ResponseWrapper.success(result);
    }

    /**
     * Maps CardInfoResult to CardInfo with calculated cardPrefix and cardStatusDisplay.
     */
    private CardInfo mapToCardInfo(CardInfoResult cardInfoResult) {
        String cardPrefix = CardPrefixUtil.extractPrefix(cardInfoResult.cardNumber());
        String cardStatusDisplay = CardStatusMapper.toDisplay(cardInfoResult.cardStatus());

        return new CardInfo(
                cardInfoResult.cardNumber(),
                cardPrefix,
                cardInfoResult.cardStatus(),
                cardStatusDisplay,
                cardInfoResult.cardFee(),
                cardInfoResult.cardAge(),
                cardInfoResult.lastUpdatedTs()
        );
    }

    /**
     * Maps ClientInfoResult to ClientInfo with caseNumber from CaseInquiryResult.
     */
    private ClientInfo mapToClientInfo(ClientInfoResult clientInfoResult, String caseNumber) {
        return new ClientInfo(
                clientInfoResult.clientId(),
                caseNumber,
                clientInfoResult.firstName(),
                clientInfoResult.lastName(),
                clientInfoResult.dateOfBirth()
        );
    }

    /**
     * Finds a matching client from the client list.
     */
    private ClientInfoResult findMatchingClient(String clientId, List<ClientInfoResult> clients) {
        return clients.stream()
                .filter(client -> client.clientId().equals(clientId))
                .findFirst()
                .orElse(clients.isEmpty() ? null : clients.get(0));
    }

    /**
     * Extracts or generates a case number for case inquiry.
     */
    private String extractOrGenerateCaseNumber(CardholderSummaryResult summaryResult) {
        // In a real scenario, this might come from the summary result or be looked up
        // For now, generate based on clientId
        return "CASE" + summaryResult.clientId().substring(Math.max(0, summaryResult.clientId().length() - 6));
    }

    /**
     * Calculates a score for the cardholder (mock implementation).
     */
    private String calculateScore(CardInfo cardInfo, ClientInfo clientInfo) {
        // Mock scoring logic: Active cards get higher scores
        return "A".equals(cardInfo.cardStatus()) ? "850" : "650";
    }

    /**
     * Masks card number for secure logging.
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        if (cleaned.length() == 16) {
            return cleaned.substring(0, 6) + "******" + cleaned.substring(12);
        }
        return cleaned.substring(0, Math.min(4, cleaned.length())) + "****";
    }

    /**
     * Internal workflow context to carry data through pipeline steps.
     */
    private record WorkflowContext(
            CardholderSearchCriteria searchCriteria,
            CardholderSummaryResult summaryResult,
            CaseInquiryResult caseResult,
            List<Cardholder> cardholders
    ) {
    }
}
