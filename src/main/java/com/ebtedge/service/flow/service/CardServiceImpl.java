package com.ebtedge.service.flow.service;

import com.ebtedge.service.flow.cardsummary.CardService;
import com.ebtedge.service.flow.cardsummary.CardholderSummaryContext;
import com.ebtedge.service.flow.cardsummary.CardholderSummaryResult;
import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.ErrorDetails;
import com.ebtedge.service.flow.domain.common.BaseResponseMetadata;
import com.ebtedge.service.flow.domain.common.CardInfoResult;
import com.ebtedge.service.flow.domain.common.ClientInfoResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CardService that provides cardholder summary information.
 * This mock implementation returns sample data for demonstration purposes.
 */
@Slf4j
@Service
public class CardServiceImpl implements CardService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public ResponseWrapper<CardholderSummaryResult> getCardholderSummary(
            CardholderSummaryContext cardholderSummaryContext) {

        try {
            // Validate input
            validateContext(cardholderSummaryContext);

            log.info("Fetching cardholder summary for cardNumber: {}, agency: {}",
                    maskCardNumber(cardholderSummaryContext.cardNumber()),
                    cardholderSummaryContext.agency());

            // Build mock response
            CardholderSummaryResult result = buildMockCardholderSummary(cardholderSummaryContext);

            log.info("Successfully retrieved cardholder summary for cardNumber: {}, clientId: {}",
                    maskCardNumber(cardholderSummaryContext.cardNumber()),
                    result.clientId());

            return ResponseWrapper.success(result);

        } catch (IllegalArgumentException e) {
            log.error("Validation error in getCardholderSummary: {}", e.getMessage());
            return ResponseWrapper.fail(new ErrorDetails("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error in getCardholderSummary", e);
            return ResponseWrapper.fail(new ErrorDetails("SERVICE_ERROR", "Failed to retrieve cardholder summary"));
        }
    }

    /**
     * Validates the cardholder summary context.
     */
    private void validateContext(CardholderSummaryContext context) {
        if (context == null) {
            throw new IllegalArgumentException("CardholderSummaryContext cannot be null");
        }
        if (context.cardNumber() == null || context.cardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Card number is required");
        }
        if (context.agency() == null || context.agency().trim().isEmpty()) {
            throw new IllegalArgumentException("Agency is required");
        }
        log.debug("Context validation passed for cardNumber: {}", maskCardNumber(context.cardNumber()));
    }

    /**
     * Builds a mock cardholder summary result for demonstration.
     */
    private CardholderSummaryResult buildMockCardholderSummary(CardholderSummaryContext context) {
        String currentTimestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String requestUuid = context.baseRequest() != null ? context.baseRequest().uuid() : UUID.randomUUID().toString();

        // Create base response metadata
        BaseResponseMetadata baseResponse = new BaseResponseMetadata(requestUuid, currentTimestamp);

        // Mock client ID based on card number (in real scenario, this comes from backend)
        String clientId = "CLI" + context.cardNumber().substring(context.cardNumber().length() - 6);

        // Create mock card info results
        List<CardInfoResult> cards = List.of(
                new CardInfoResult(
                        context.cardNumber(),
                        "A", // Active status
                        "0.00",
                        "365",
                        currentTimestamp
                ),
                new CardInfoResult(
                        generateSecondaryCardNumber(context.cardNumber()),
                        "I", // Inactive status
                        "2.50",
                        "730",
                        currentTimestamp
                )
        );

        // Create mock client info results
        List<ClientInfoResult> clients = List.of(
                new ClientInfoResult(
                        clientId,
                        null, // caseNumber will be populated from CaseInquiry
                        "John",
                        "Doe",
                        "1990-01-15"
                )
        );

        log.debug("Built mock cardholder summary with {} cards and {} clients",
                cards.size(), clients.size());

        return new CardholderSummaryResult(
                baseResponse,
                clientId,
                context.cardNumber(),
                cards,
                clients
        );
    }

    /**
     * Generates a secondary card number for testing (replaces last 4 digits).
     */
    private String generateSecondaryCardNumber(String primaryCardNumber) {
        if (primaryCardNumber.length() >= 4) {
            return primaryCardNumber.substring(0, primaryCardNumber.length() - 4) + "9999";
        }
        return primaryCardNumber;
    }

    /**
     * Masks card number for secure logging (shows first 6 and last 4 digits).
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
}
