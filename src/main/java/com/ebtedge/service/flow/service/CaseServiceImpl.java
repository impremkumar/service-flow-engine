package com.ebtedge.service.flow.service;

import com.ebtedge.service.flow.caseinquiry.CaseInquiryContext;
import com.ebtedge.service.flow.caseinquiry.CaseInquiryResult;
import com.ebtedge.service.flow.caseinquiry.CaseService;
import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.ErrorDetails;
import com.ebtedge.service.flow.domain.common.ClientInfoResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of CaseService that provides case inquiry information.
 * This mock implementation returns sample case data for demonstration purposes.
 */
@Slf4j
@Service
public class CaseServiceImpl implements CaseService {

    @Override
    public ResponseWrapper<CaseInquiryResult> caseInquiry(CaseInquiryContext caseInquiryContext) {

        try {
            // Validate input
            validateContext(caseInquiryContext);

            log.info("Performing case inquiry for caseNumber: {}, agency: {}",
                    caseInquiryContext.caseNumber(),
                    caseInquiryContext.agency());

            // Build mock response
            CaseInquiryResult result = buildMockCaseInquiry(caseInquiryContext);

            log.info("Successfully retrieved case inquiry for caseNumber: {}, found {} clients",
                    caseInquiryContext.caseNumber(),
                    result.clients().size());

            return ResponseWrapper.success(result);

        } catch (IllegalArgumentException e) {
            log.error("Validation error in caseInquiry: {}", e.getMessage());
            return ResponseWrapper.fail(new ErrorDetails("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error in caseInquiry", e);
            return ResponseWrapper.fail(new ErrorDetails("SERVICE_ERROR", "Failed to retrieve case inquiry"));
        }
    }

    /**
     * Validates the case inquiry context.
     */
    private void validateContext(CaseInquiryContext context) {
        if (context == null) {
            throw new IllegalArgumentException("CaseInquiryContext cannot be null");
        }
        if (context.caseNumber() == null || context.caseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Case number is required");
        }
        if (context.agency() == null || context.agency().trim().isEmpty()) {
            throw new IllegalArgumentException("Agency is required");
        }
        log.debug("Context validation passed for caseNumber: {}", context.caseNumber());
    }

    /**
     * Builds a mock case inquiry result for demonstration.
     */
    private CaseInquiryResult buildMockCaseInquiry(CaseInquiryContext context) {
        // Generate mock client ID based on case number
        String clientId = "CLI" + context.caseNumber().substring(Math.max(0, context.caseNumber().length() - 6));

        // Create mock client info results
        List<ClientInfoResult> clients = List.of(
                new ClientInfoResult(
                        clientId,
                        context.caseNumber(),
                        "John",
                        "Doe",
                        "1990-01-15"
                ),
                new ClientInfoResult(
                        clientId + "02",
                        context.caseNumber(),
                        "Jane",
                        "Doe",
                        "1992-03-20"
                )
        );

        log.debug("Built mock case inquiry with caseNumber: {}, {} clients",
                context.caseNumber(), clients.size());

        return new CaseInquiryResult(context.caseNumber(), clients);
    }
}
