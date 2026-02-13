package com.ebtedge.service.flow.caseinquiry;

import com.ebtedge.service.flow.caseinquiry.CaseInquiryContext;
import com.ebtedge.service.flow.caseinquiry.CaseInquiryResult;
import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.common.BaseRequestMetaData;
import com.ebtedge.service.flow.service.CaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaseServiceImplTest {

    private CaseServiceImpl caseService;

    @BeforeEach
    void setUp() {
        caseService = new CaseServiceImpl();
    }

    @Test
    void testCaseInquiry_validContext_returnsSuccess() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CaseInquiryContext context = new CaseInquiryContext(
                baseRequest,
                "CASE123456",
                "AG001"
        );

        // When
        ResponseWrapper<CaseInquiryResult> response = caseService.caseInquiry(context);

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());

        CaseInquiryResult result = response.getData();
        assertEquals("CASE123456", result.caseNumber());
        assertFalse(result.clients().isEmpty());
        assertEquals(2, result.clients().size());
    }

    @Test
    void testCaseInquiry_clientsHaveCaseNumber_success() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CaseInquiryContext context = new CaseInquiryContext(
                baseRequest,
                "CASE123456",
                "AG001"
        );

        // When
        ResponseWrapper<CaseInquiryResult> response = caseService.caseInquiry(context);

        // Then
        assertTrue(response.isSuccess());
        CaseInquiryResult result = response.getData();

        // Verify all clients have the case number
        result.clients().forEach(client -> {
            assertEquals("CASE123456", client.caseNumber());
            assertNotNull(client.clientId());
            assertNotNull(client.firstName());
            assertNotNull(client.lastName());
            assertNotNull(client.dateOfBirth());
        });
    }

    @Test
    void testCaseInquiry_multipleClients_correctData() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CaseInquiryContext context = new CaseInquiryContext(
                baseRequest,
                "CASE123456",
                "AG001"
        );

        // When
        ResponseWrapper<CaseInquiryResult> response = caseService.caseInquiry(context);

        // Then
        assertTrue(response.isSuccess());
        CaseInquiryResult result = response.getData();
        assertEquals(2, result.clients().size());

        var client1 = result.clients().get(0);
        assertEquals("John", client1.firstName());
        assertEquals("Doe", client1.lastName());

        var client2 = result.clients().get(1);
        assertEquals("Jane", client2.firstName());
        assertEquals("Doe", client2.lastName());
    }

    @Test
    void testCaseInquiry_nullContext_returnsError() {
        // When
        ResponseWrapper<CaseInquiryResult> response = caseService.caseInquiry(null);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("cannot be null"));
    }

    @Test
    void testCaseInquiry_nullCaseNumber_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CaseInquiryContext context = new CaseInquiryContext(
                baseRequest,
                null,
                "AG001"
        );

        // When
        ResponseWrapper<CaseInquiryResult> response = caseService.caseInquiry(context);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("Case number is required"));
    }

    @Test
    void testCaseInquiry_emptyCaseNumber_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CaseInquiryContext context = new CaseInquiryContext(
                baseRequest,
                "   ",
                "AG001"
        );

        // When
        ResponseWrapper<CaseInquiryResult> response = caseService.caseInquiry(context);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
    }

    @Test
    void testCaseInquiry_nullAgency_returnsError() {
        // Given
        BaseRequestMetaData baseRequest = new BaseRequestMetaData("TERM001", "uuid-123", "USER001");
        CaseInquiryContext context = new CaseInquiryContext(
                baseRequest,
                "CASE123456",
                null
        );

        // When
        ResponseWrapper<CaseInquiryResult> response = caseService.caseInquiry(context);

        // Then
        assertFalse(response.isSuccess());
        assertEquals("VALIDATION_ERROR", response.getError().errorCode());
        assertTrue(response.getError().message().contains("Agency is required"));
    }
}
