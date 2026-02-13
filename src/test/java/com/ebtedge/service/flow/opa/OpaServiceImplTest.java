package com.ebtedge.service.flow.opa;

import com.ebtedge.service.flow.cardsummary.CardService;
import com.ebtedge.service.flow.cardsummary.CardholderSummaryContext;
import com.ebtedge.service.flow.cardsummary.CardholderSummaryResult;
import com.ebtedge.service.flow.caseinquiry.CaseInquiryContext;
import com.ebtedge.service.flow.caseinquiry.CaseInquiryResult;
import com.ebtedge.service.flow.caseinquiry.CaseService;
import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.core.WorkflowPipelineFactory;
import com.ebtedge.service.flow.domain.CardholderSearchCriteria;
import com.ebtedge.service.flow.domain.ErrorDetails;
import com.ebtedge.service.flow.domain.CardholderSearchResult;
import com.ebtedge.service.flow.domain.common.BaseRequestMetaData;
import com.ebtedge.service.flow.domain.common.BaseResponseMetadata;
import com.ebtedge.service.flow.domain.common.CardInfoResult;
import com.ebtedge.service.flow.domain.common.ClientInfoResult;
import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import com.ebtedge.service.flow.service.OpaServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpaServiceImplTest {

    @Mock
    private CardService cardService;

    @Mock
    private CaseService caseService;

    private WorkflowPipelineFactory pipelineFactory;
    private OpaServiceImpl opaService;

    @BeforeEach
    void setUp() {
        ServiceFlowProperties properties = new ServiceFlowProperties();
        properties.setMetricsEnabled(true);
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        pipelineFactory = new WorkflowPipelineFactory(properties, meterRegistry);

        opaService = new OpaServiceImpl(cardService, caseService, pipelineFactory);
    }

    @Test
    void testCardholderSearch_validCriteria_returnsSuccess() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSearchCriteria criteria = new CardholderSearchCriteria(
                baseRequest,
                "4111111111111111",
                "AG001"
        );

        // Mock CardService response
        CardholderSummaryResult summaryResult = new CardholderSummaryResult(
                new BaseResponseMetadata("uuid-123", "2025-01-15T10:00:00Z"),
                "CLI123456",
                "4111111111111111",
                List.of(new CardInfoResult("4111111111111111", "A", "0.00", "365", "2025-01-15T10:00:00Z")),
                List.of(new ClientInfoResult("CLI123456", null, "John", "Doe", "1990-01-15"))
        );
        when(cardService.getCardholderSummary(any(CardholderSummaryContext.class)))
                .thenReturn(ResponseWrapper.success(summaryResult));

        // Mock CaseService response
        CaseInquiryResult caseResult = new CaseInquiryResult(
                "CASE123456",
                List.of(new ClientInfoResult("CLI123456", "CASE123456", "John", "Doe", "1990-01-15"))
        );
        when(caseService.caseInquiry(any(CaseInquiryContext.class)))
                .thenReturn(ResponseWrapper.success(caseResult));

        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(criteria);

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());

        CardholderSearchResult result = response.getData();
        assertNotNull(result.baseResponse());
        assertEquals("uuid-123", result.baseResponse().uuid());
        assertFalse(result.cardholderList().isEmpty());
        assertEquals(1, result.cardholderList().size());

        var cardholder = result.cardholderList().get(0);
        assertNotNull(cardholder.cardholderId());
        assertNotNull(cardholder.score());
        assertNotNull(cardholder.clientInfo());
        assertNotNull(cardholder.cardInfo());

        // Verify card info mapping
        assertEquals("4111111111111111", cardholder.cardInfo().cardNumber());
        assertEquals("411111", cardholder.cardInfo().cardPrefix());
        assertEquals("A", cardholder.cardInfo().cardStatus());
        assertEquals("Active", cardholder.cardInfo().cardStatusDisplay());

        // Verify client info mapping
        assertEquals("CLI123456", cardholder.clientInfo().clientId());
        assertEquals("CASE123456", cardholder.clientInfo().caseNumber());
        assertEquals("John", cardholder.clientInfo().firstName());
        assertEquals("Doe", cardholder.clientInfo().lastName());

        // Verify service interactions
        verify(cardService, times(1)).getCardholderSummary(any(CardholderSummaryContext.class));
        verify(caseService, times(1)).caseInquiry(any(CaseInquiryContext.class));
    }

    @Test
    void testCardholderSearch_multipleCards_returnsMultipleCardholders() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSearchCriteria criteria = new CardholderSearchCriteria(
                baseRequest,
                "4111111111111111",
                "AG001"
        );

        // Mock CardService response with multiple cards
        CardholderSummaryResult summaryResult = new CardholderSummaryResult(
                new BaseResponseMetadata("uuid-123", "2025-01-15T10:00:00Z"),
                "CLI123456",
                "4111111111111111",
                List.of(
                        new CardInfoResult("4111111111111111", "A", "0.00", "365", "2025-01-15T10:00:00Z"),
                        new CardInfoResult("5500000000000004", "I", "2.50", "730", "2025-01-15T10:00:00Z")
                ),
                List.of(new ClientInfoResult("CLI123456", null, "John", "Doe", "1990-01-15"))
        );
        when(cardService.getCardholderSummary(any(CardholderSummaryContext.class)))
                .thenReturn(ResponseWrapper.success(summaryResult));

        // Mock CaseService response
        CaseInquiryResult caseResult = new CaseInquiryResult(
                "CASE123456",
                List.of(new ClientInfoResult("CLI123456", "CASE123456", "John", "Doe", "1990-01-15"))
        );
        when(caseService.caseInquiry(any(CaseInquiryContext.class)))
                .thenReturn(ResponseWrapper.success(caseResult));

        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(criteria);

        // Then
        assertTrue(response.isSuccess());
        CardholderSearchResult result = response.getData();
        assertEquals(2, result.cardholderList().size());

        // Verify first cardholder (Active card)
        var cardholder1 = result.cardholderList().get(0);
        assertEquals("Active", cardholder1.cardInfo().cardStatusDisplay());
        assertEquals("850", cardholder1.score()); // Active cards get 850 score

        // Verify second cardholder (Inactive card)
        var cardholder2 = result.cardholderList().get(1);
        assertEquals("Inactive", cardholder2.cardInfo().cardStatusDisplay());
        assertEquals("650", cardholder2.score()); // Inactive cards get 650 score
    }

    @Test
    void testCardholderSearch_cardServiceError_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSearchCriteria criteria = new CardholderSearchCriteria(
                baseRequest,
                "4111111111111111",
                "AG001"
        );

        // Mock CardService error
        when(cardService.getCardholderSummary(any(CardholderSummaryContext.class)))
                .thenReturn(ResponseWrapper.fail(new ErrorDetails("CARD_SERVICE_ERROR", "Failed to fetch card summary")));

        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(criteria);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("SERVICE_ERROR", response.getError().errorCode());

        // Verify caseService was never called
        verify(cardService, times(1)).getCardholderSummary(any(CardholderSummaryContext.class));
        verify(caseService, never()).caseInquiry(any(CaseInquiryContext.class));
    }

    @Test
    void testCardholderSearch_caseServiceError_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSearchCriteria criteria = new CardholderSearchCriteria(
                baseRequest,
                "4111111111111111",
                "AG001"
        );

        // Mock CardService success
        CardholderSummaryResult summaryResult = new CardholderSummaryResult(
                new BaseResponseMetadata("uuid-123", "2025-01-15T10:00:00Z"),
                "CLI123456",
                "4111111111111111",
                List.of(new CardInfoResult("4111111111111111", "A", "0.00", "365", "2025-01-15T10:00:00Z")),
                List.of(new ClientInfoResult("CLI123456", null, "John", "Doe", "1990-01-15"))
        );
        when(cardService.getCardholderSummary(any(CardholderSummaryContext.class)))
                .thenReturn(ResponseWrapper.success(summaryResult));

        // Mock CaseService error
        when(caseService.caseInquiry(any(CaseInquiryContext.class)))
                .thenReturn(ResponseWrapper.fail(new ErrorDetails("CASE_SERVICE_ERROR", "Failed to fetch case inquiry")));

        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(criteria);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("SERVICE_ERROR", response.getError().errorCode());

        // Verify both services were called
        verify(cardService, times(1)).getCardholderSummary(any(CardholderSummaryContext.class));
        verify(caseService, times(1)).caseInquiry(any(CaseInquiryContext.class));
    }

    @Test
    void testCardholderSearch_nullCriteria_returnsError() {
        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(null);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("cannot be null"));

        // Verify no service calls
        verify(cardService, never()).getCardholderSummary(any());
        verify(caseService, never()).caseInquiry(any());
    }

    @Test
    void testCardholderSearch_nullCardNumber_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSearchCriteria criteria = new CardholderSearchCriteria(
                baseRequest,
                null,
                "AG001"
        );

        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(criteria);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("Card number is required"));
    }

    @Test
    void testCardholderSearch_nullAgency_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSearchCriteria criteria = new CardholderSearchCriteria(
                baseRequest,
                "4111111111111111",
                null
        );

        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(criteria);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("Agency is required"));
    }

    @Test
    void testCardholderSearch_cardholderIdEncryption_correct() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSearchCriteria criteria = new CardholderSearchCriteria(
                baseRequest,
                "4111111111111111",
                "AG001"
        );

        // Mock services
        CardholderSummaryResult summaryResult = new CardholderSummaryResult(
                new BaseResponseMetadata("uuid-123", "2025-01-15T10:00:00Z"),
                "CLI123456",
                "4111111111111111",
                List.of(new CardInfoResult("4111111111111111", "A", "0.00", "365", "2025-01-15T10:00:00Z")),
                List.of(new ClientInfoResult("CLI123456", null, "John", "Doe", "1990-01-15"))
        );
        when(cardService.getCardholderSummary(any())).thenReturn(ResponseWrapper.success(summaryResult));

        CaseInquiryResult caseResult = new CaseInquiryResult(
                "CASE123456",
                List.of(new ClientInfoResult("CLI123456", "CASE123456", "John", "Doe", "1990-01-15"))
        );
        when(caseService.caseInquiry(any())).thenReturn(ResponseWrapper.success(caseResult));

        // When
        ResponseWrapper<CardholderSearchResult> response = opaService.cardholderSearch(criteria);

        // Then
        assertTrue(response.isSuccess());
        var cardholder = response.getData().cardholderList().get(0);
        assertNotNull(cardholder.cardholderId());
        assertTrue(cardholder.cardholderId().length() > 0);
        // Verify it's base64 encoded (doesn't contain special characters except =)
        assertTrue(cardholder.cardholderId().matches("^[A-Za-z0-9+/=]+$"));
    }
}
