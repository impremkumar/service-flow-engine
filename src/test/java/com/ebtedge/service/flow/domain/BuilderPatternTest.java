package com.ebtedge.service.flow.domain;

import com.ebtedge.service.flow.cardsummary.CardholderSummaryContext;
import com.ebtedge.service.flow.caseinquiry.CaseInquiryContext;
import com.ebtedge.service.flow.domain.common.BaseRequestMetaData;
import com.ebtedge.service.flow.domain.common.BaseResponseMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for builder patterns across all record classes.
 */
class BuilderPatternTest {

    @Test
    void testBaseRequestMetaData_builder_success() {
        // When
        BaseRequestMetaData metadata = BaseRequestMetaData.builder()
                .terminalId("TERM001")
                .uuid("uuid-123")
                .userId("USER001")
                .build();

        // Then
        assertEquals("TERM001", metadata.terminalId());
        assertEquals("uuid-123", metadata.uuid());
        assertEquals("USER001", metadata.userId());
    }

    @Test
    void testBaseResponseMetadata_builder_success() {
        // When
        BaseResponseMetadata metadata = BaseResponseMetadata.builder()
                .uuid("uuid-123")
                .responseTime("2025-01-15T10:00:00Z")
                .build();

        // Then
        assertEquals("uuid-123", metadata.uuid());
        assertEquals("2025-01-15T10:00:00Z", metadata.responseTime());
    }

    @Test
    void testCardholderSearchCriteria_builder_success() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");

        // When
        CardholderSearchCriteria criteria = CardholderSearchCriteria.builder()
                .baseRequest(baseRequest)
                .cardNumber("4111111111111111")
                .agency("AG001")
                .build();

        // Then
        assertEquals(baseRequest, criteria.baseRequest());
        assertEquals("4111111111111111", criteria.cardNumber());
        assertEquals("AG001", criteria.agency());
    }

    @Test
    void testCardholderSummaryContext_builder_success() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");

        // When
        CardholderSummaryContext context = CardholderSummaryContext.builder()
                .baseRequest(baseRequest)
                .cardNumber("4111111111111111")
                .agency("AG001")
                .build();

        // Then
        assertEquals(baseRequest, context.baseRequest());
        assertEquals("4111111111111111", context.cardNumber());
        assertEquals("AG001", context.agency());
    }

    @Test
    void testCaseInquiryContext_builder_success() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");

        // When
        CaseInquiryContext context = CaseInquiryContext.builder()
                .baseRequest(baseRequest)
                .caseNumber("CASE123456")
                .agency("AG001")
                .build();

        // Then
        assertEquals(baseRequest, context.baseRequest());
        assertEquals("CASE123456", context.caseNumber());
        assertEquals("AG001", context.agency());
    }

    @Test
    void testCardInfo_builder_success() {
        // When
        CardInfo cardInfo = CardInfo.builder()
                .cardNumber("4111111111111111")
                .cardPrefix("411111")
                .cardStatus("A")
                .cardStatusDisplay("Active")
                .cardFee("0.00")
                .cardAge("365")
                .lastUpdatedTs("2025-01-15T10:00:00Z")
                .build();

        // Then
        assertEquals("4111111111111111", cardInfo.cardNumber());
        assertEquals("411111", cardInfo.cardPrefix());
        assertEquals("A", cardInfo.cardStatus());
        assertEquals("Active", cardInfo.cardStatusDisplay());
        assertEquals("0.00", cardInfo.cardFee());
        assertEquals("365", cardInfo.cardAge());
        assertEquals("2025-01-15T10:00:00Z", cardInfo.lastUpdatedTs());
    }

    @Test
    void testClientInfo_builder_success() {
        // When
        ClientInfo clientInfo = ClientInfo.builder()
                .clientId("CLI123456")
                .caseNumber("CASE123456")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth("1990-01-15")
                .build();

        // Then
        assertEquals("CLI123456", clientInfo.clientId());
        assertEquals("CASE123456", clientInfo.caseNumber());
        assertEquals("John", clientInfo.firstName());
        assertEquals("Doe", clientInfo.lastName());
        assertEquals("1990-01-15", clientInfo.dateOfBirth());
    }

    @Test
    void testCardholder_builder_success() {
        // Given
        ClientInfo clientInfo = new ClientInfo("CLI123456", "CASE123456", "John", "Doe", "1990-01-15");
        CardInfo cardInfo = new CardInfo("4111111111111111", "411111", "A", "Active",
                                          "0.00", "365", "2025-01-15T10:00:00Z");

        // When
        Cardholder cardholder = Cardholder.builder()
                .cardholderId("encrypted-id")
                .score("850")
                .clientInfo(clientInfo)
                .cardInfo(cardInfo)
                .build();

        // Then
        assertEquals("encrypted-id", cardholder.cardholderId());
        assertEquals("850", cardholder.score());
        assertEquals(clientInfo, cardholder.clientInfo());
        assertEquals(cardInfo, cardholder.cardInfo());
    }

    @Test
    void testCardholderSearchResult_builder_success() {
        // Given
        BaseResponseMetadata baseResponse = new BaseResponseMetadata("uuid-123", "2025-01-15T10:00:00Z");
        ClientInfo clientInfo = new ClientInfo("CLI123456", "CASE123456", "John", "Doe", "1990-01-15");
        CardInfo cardInfo = new CardInfo("4111111111111111", "411111", "A", "Active",
                                          "0.00", "365", "2025-01-15T10:00:00Z");
        Cardholder cardholder = new Cardholder("encrypted-id", "850", clientInfo, cardInfo);
        List<Cardholder> cardholders = List.of(cardholder);

        // When
        CardholderSearchResult result = CardholderSearchResult.builder()
                .baseResponse(baseResponse)
                .cardholderList(cardholders)
                .offset("0")
                .build();

        // Then
        assertEquals(baseResponse, result.baseResponse());
        assertEquals(cardholders, result.cardholderList());
        assertEquals("0", result.offset());
        assertEquals(1, result.cardholderList().size());
    }

    @Test
    void testBuilderPattern_nullValues_allowed() {
        // When - builders should handle null values
        CardInfo cardInfo = CardInfo.builder()
                .cardNumber(null)
                .cardPrefix(null)
                .cardStatus(null)
                .cardStatusDisplay(null)
                .cardFee(null)
                .cardAge(null)
                .lastUpdatedTs(null)
                .build();

        // Then - should create object with null fields
        assertNull(cardInfo.cardNumber());
        assertNull(cardInfo.cardPrefix());
        assertNull(cardInfo.cardStatus());
    }

    @Test
    void testBuilderPattern_multipleBuildsFromSameBuilder() {
        // Given
        CardInfo.Builder builder = CardInfo.builder()
                .cardNumber("4111111111111111")
                .cardPrefix("411111")
                .cardStatus("A")
                .cardStatusDisplay("Active")
                .cardFee("0.00")
                .cardAge("365")
                .lastUpdatedTs("2025-01-15T10:00:00Z");

        // When - build multiple times
        CardInfo cardInfo1 = builder.build();
        CardInfo cardInfo2 = builder.build();

        // Then - both should be equal
        assertEquals(cardInfo1.cardNumber(), cardInfo2.cardNumber());
        assertEquals(cardInfo1.cardPrefix(), cardInfo2.cardPrefix());
        assertEquals(cardInfo1.cardStatus(), cardInfo2.cardStatus());
    }
}
