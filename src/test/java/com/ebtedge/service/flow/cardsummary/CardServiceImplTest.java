package com.ebtedge.service.flow.cardsummary;

import com.ebtedge.service.flow.cardsummary.CardholderSummaryContext;
import com.ebtedge.service.flow.cardsummary.CardholderSummaryResult;
import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.common.BaseRequestMetaData;
import com.ebtedge.service.flow.service.CardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardServiceImplTest {

    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardServiceImpl();
    }

    @Test
    void testGetCardholderSummary_validContext_returnsSuccess() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSummaryContext context = new CardholderSummaryContext(
                baseRequest,
                "1234567890123456",
                "AG001"
        );

        // When
        ResponseWrapper<CardholderSummaryResult> response = cardService.getCardholderSummary(context);

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());

        CardholderSummaryResult result = response.getData();
        assertEquals("1234567890123456", result.cardNumber());
        assertNotNull(result.clientId());
        assertTrue(result.clientId().startsWith("CLI"));
        assertFalse(result.cards().isEmpty());
        assertFalse(result.clients().isEmpty());
        assertNotNull(result.baseResponse());
        assertEquals("uuid-123", result.baseResponse().uuid());
    }

    @Test
    void testGetCardholderSummary_multipleCards_returnsMultipleCards() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSummaryContext context = new CardholderSummaryContext(
                baseRequest,
                "1234567890123456",
                "AG001"
        );

        // When
        ResponseWrapper<CardholderSummaryResult> response = cardService.getCardholderSummary(context);

        // Then
        assertTrue(response.isSuccess());
        CardholderSummaryResult result = response.getData();
        assertEquals(2, result.cards().size());

        // Verify first card is active
        assertEquals("A", result.cards().get(0).cardStatus());
        assertEquals("1234567890123456", result.cards().get(0).cardNumber());

        // Verify second card is inactive
        assertEquals("I", result.cards().get(1).cardStatus());
    }

    @Test
    void testGetCardholderSummary_nullContext_returnsError() {
        // When
        ResponseWrapper<CardholderSummaryResult> response = cardService.getCardholderSummary(null);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("cannot be null"));
    }

    @Test
    void testGetCardholderSummary_nullCardNumber_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSummaryContext context = new CardholderSummaryContext(
                baseRequest,
                null,
                "AG001"
        );

        // When
        ResponseWrapper<CardholderSummaryResult> response = cardService.getCardholderSummary(context);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("Card number is required"));
    }

    @Test
    void testGetCardholderSummary_emptyCardNumber_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSummaryContext context = new CardholderSummaryContext(
                baseRequest,
                "   ",
                "AG001"
        );

        // When
        ResponseWrapper<CardholderSummaryResult> response = cardService.getCardholderSummary(context);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
    }

    @Test
    void testGetCardholderSummary_nullAgency_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSummaryContext context = new CardholderSummaryContext(
                baseRequest,
                "1234567890123456",
                null
        );

        // When
        ResponseWrapper<CardholderSummaryResult> response = cardService.getCardholderSummary(context);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("Agency is required"));
    }

    @Test
    void testGetCardholderSummary_clientData_correctlyPopulated() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CardholderSummaryContext context = new CardholderSummaryContext(
                baseRequest,
                "1234567890123456",
                "AG001"
        );

        // When
        ResponseWrapper<CardholderSummaryResult> response = cardService.getCardholderSummary(context);

        // Then
        assertTrue(response.isSuccess());
        CardholderSummaryResult result = response.getData();
        assertEquals(1, result.clients().size());

        var client = result.clients().get(0);
        assertEquals("John", client.firstName());
        assertEquals("Doe", client.lastName());
        assertNotNull(client.dateOfBirth());
        assertEquals(result.clientId(), client.clientId());
    }
}
