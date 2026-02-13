package com.ebtedge.service.flow.service;

import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.Demographics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MockServiceBTest {

    private MockServiceB serviceB;

    @BeforeEach
    void setUp() {
        serviceB = new MockServiceB();
    }

    @Test
    void testGetDemographics_Success() {
        ResponseWrapper<Demographics> result = serviceB.getDemographics("CUST-7788");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("Jane Doe", result.getData().name());
        assertEquals("jane.doe@example.com", result.getData().email());
        assertEquals("New York", result.getData().city());
        assertNull(result.getError());
    }

    @Test
    void testGetDemographics_NullClientId() {
        ResponseWrapper<Demographics> result = serviceB.getDemographics(null);

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertNotNull(result.getError());
        assertEquals("B_ERR_99", result.getError().errorCode());
        assertEquals("Client ID is required", result.getError().message());
    }

    @Test
    void testGetDemographics_EmptyClientId() {
        ResponseWrapper<Demographics> result = serviceB.getDemographics("");

        assertFalse(result.isSuccess());
        assertEquals("B_ERR_99", result.getError().errorCode());
    }

    @Test
    void testGetDemographics_DifferentValidClientIds() {
        ResponseWrapper<Demographics> result1 = serviceB.getDemographics("CUST-001");
        ResponseWrapper<Demographics> result2 = serviceB.getDemographics("CUST-999");

        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertEquals(result1.getData().name(), result2.getData().name());
        assertEquals(result1.getData().email(), result2.getData().email());
        assertEquals(result1.getData().city(), result2.getData().city());
    }

    @Test
    void testGetDemographics_ValidClientIdWithSpaces() {
        ResponseWrapper<Demographics> result = serviceB.getDemographics("CUST 1234");

        assertTrue(result.isSuccess());
        assertEquals("Jane Doe", result.getData().name());
    }

    @Test
    void testGetDemographics_VerifyAllFields() {
        ResponseWrapper<Demographics> result = serviceB.getDemographics("ANY-ID");

        assertTrue(result.isSuccess());
        Demographics data = result.getData();
        assertNotNull(data.name());
        assertNotNull(data.email());
        assertNotNull(data.city());
        assertFalse(data.name().isEmpty());
        assertFalse(data.email().isEmpty());
        assertFalse(data.city().isEmpty());
    }
}
