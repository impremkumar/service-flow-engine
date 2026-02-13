package com.ebtedge.service.flow.service;

import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.Balance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MockServiceATest {

    private MockServiceA serviceA;

    @BeforeEach
    void setUp() {
        serviceA = new MockServiceA();
    }

    @Test
    void testGetBalance_Success() {
        ResponseWrapper<Balance> result = serviceA.getBalance("12345");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("CUST-7788", result.getData().clientId());
        assertEquals(1250.50, result.getData().amount());
        assertNull(result.getError());
    }

    @Test
    void testGetBalance_InvalidAccountId() {
        ResponseWrapper<Balance> result = serviceA.getBalance("invalid");

        assertFalse(result.isSuccess());
        assertNull(result.getData());
        assertNotNull(result.getError());
        assertEquals("A_ERR_01", result.getError().errorCode());
        assertEquals("Account not found in Ledger", result.getError().message());
    }

    @Test
    void testGetBalance_InvalidAccountId_CaseInsensitive() {
        ResponseWrapper<Balance> result = serviceA.getBalance("INVALID");

        assertFalse(result.isSuccess());
        assertEquals("A_ERR_01", result.getError().errorCode());
    }

    @Test
    void testGetBalance_InvalidAccountId_MixedCase() {
        ResponseWrapper<Balance> result = serviceA.getBalance("InVaLiD");

        assertFalse(result.isSuccess());
        assertEquals("A_ERR_01", result.getError().errorCode());
    }

    @Test
    void testGetBalance_DifferentValidAccountIds() {
        // Service returns same mock data for any valid ID
        ResponseWrapper<Balance> result1 = serviceA.getBalance("ACC-001");
        ResponseWrapper<Balance> result2 = serviceA.getBalance("ACC-999");

        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertEquals(result1.getData().clientId(), result2.getData().clientId());
        assertEquals(result1.getData().amount(), result2.getData().amount());
    }

    @Test
    void testGetBalance_EmptyString() {
        ResponseWrapper<Balance> result = serviceA.getBalance("");

        assertTrue(result.isSuccess());
        assertEquals("CUST-7788", result.getData().clientId());
    }

    @Test
    void testGetBalance_NumericAccountId() {
        ResponseWrapper<Balance> result = serviceA.getBalance("999888777");

        assertTrue(result.isSuccess());
        assertEquals(1250.50, result.getData().amount());
    }
}
