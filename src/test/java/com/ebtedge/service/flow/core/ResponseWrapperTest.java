package com.ebtedge.service.flow.core;

import com.ebtedge.service.flow.domain.ErrorDetails;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponseWrapperTest {

    @Test
    void testSuccessWrapper() {
        String testData = "test-data";
        ResponseWrapper<String> wrapper = ResponseWrapper.success(testData);

        assertTrue(wrapper.isSuccess());
        assertEquals(testData, wrapper.getData());
        assertNull(wrapper.getError());
    }

    @Test
    void testFailWrapper() {
        ErrorDetails error = new ErrorDetails("ERR_001", "Test error");
        ResponseWrapper<String> wrapper = ResponseWrapper.fail(error);

        assertFalse(wrapper.isSuccess());
        assertNull(wrapper.getData());
        assertEquals(error, wrapper.getError());
    }

    @Test
    void testSuccessWrapperWithNullData() {
        ResponseWrapper<String> wrapper = ResponseWrapper.success(null);

        assertTrue(wrapper.isSuccess());
        assertNull(wrapper.getData());
        assertNull(wrapper.getError());
    }

    @Test
    void testFailWrapperWithNullError() {
        ResponseWrapper<String> wrapper = ResponseWrapper.fail(null);

        assertFalse(wrapper.isSuccess());
        assertNull(wrapper.getData());
        assertNull(wrapper.getError());
    }

    @Test
    void testWrapperImmutability() {
        String data = "immutable";
        ResponseWrapper<String> wrapper = ResponseWrapper.success(data);

        // Verify getters return same values
        assertEquals(data, wrapper.getData());
        assertEquals(data, wrapper.getData());
        assertTrue(wrapper.isSuccess());
    }

    @Test
    void testSuccessWrapperWithComplexObject() {
        ErrorDetails complexData = new ErrorDetails("CODE", "Message");
        ResponseWrapper<ErrorDetails> wrapper = ResponseWrapper.success(complexData);

        assertTrue(wrapper.isSuccess());
        assertEquals(complexData, wrapper.getData());
        assertEquals("CODE", wrapper.getData().errorCode());
        assertEquals("Message", wrapper.getData().message());
    }

    @Test
    void testFailWrapperPreservesErrorDetails() {
        ErrorDetails error = new ErrorDetails("ERR_999", "Critical failure");
        ResponseWrapper<Integer> wrapper = ResponseWrapper.fail(error);

        assertFalse(wrapper.isSuccess());
        assertEquals("ERR_999", wrapper.getError().errorCode());
        assertEquals("Critical failure", wrapper.getError().message());
    }

    @Test
    void testMap_Success() {
        ResponseWrapper<Integer> wrapper = ResponseWrapper.success(10);
        ResponseWrapper<String> mapped = wrapper.map(i -> "Number: " + i);

        assertTrue(mapped.isSuccess());
        assertEquals("Number: 10", mapped.getData());
    }

    @Test
    void testMap_Failure() {
        ErrorDetails error = new ErrorDetails("ERR", "Failed");
        ResponseWrapper<Integer> wrapper = ResponseWrapper.fail(error);
        ResponseWrapper<String> mapped = wrapper.map(i -> "Number: " + i);

        assertFalse(mapped.isSuccess());
        assertEquals(error, mapped.getError());
    }

    @Test
    void testFlatMap_Success() {
        ResponseWrapper<Integer> wrapper = ResponseWrapper.success(5);
        ResponseWrapper<Integer> result = wrapper.flatMap(i ->
            i > 0 ? ResponseWrapper.success(i * 2) : ResponseWrapper.fail(new ErrorDetails("NEG", "Negative")));

        assertTrue(result.isSuccess());
        assertEquals(10, result.getData());
    }

    @Test
    void testFlatMap_PropagatesFailure() {
        ErrorDetails error = new ErrorDetails("INITIAL", "Initial error");
        ResponseWrapper<Integer> wrapper = ResponseWrapper.fail(error);
        ResponseWrapper<Integer> result = wrapper.flatMap(i -> ResponseWrapper.success(i * 2));

        assertFalse(result.isSuccess());
        assertEquals(error, result.getError());
    }

    @Test
    void testOrElse_Success() {
        ResponseWrapper<String> wrapper = ResponseWrapper.success("actual");
        String result = wrapper.orElse("default");

        assertEquals("actual", result);
    }

    @Test
    void testOrElse_Failure() {
        ResponseWrapper<String> wrapper = ResponseWrapper.fail(new ErrorDetails("ERR", "Failed"));
        String result = wrapper.orElse("default");

        assertEquals("default", result);
    }

    @Test
    void testRecover_Success() {
        ResponseWrapper<String> wrapper = ResponseWrapper.success("data");
        ResponseWrapper<String> result = wrapper.recover(err -> "recovered");

        assertTrue(result.isSuccess());
        assertEquals("data", result.getData());
    }

    @Test
    void testRecover_Failure() {
        ErrorDetails error = new ErrorDetails("ERR", "Failed");
        ResponseWrapper<String> wrapper = ResponseWrapper.fail(error);
        ResponseWrapper<String> result = wrapper.recover(err -> "recovered from: " + err.errorCode());

        assertTrue(result.isSuccess());
        assertEquals("recovered from: ERR", result.getData());
    }

    @Test
    void testOnSuccess_ExecutesOnSuccess() {
        ResponseWrapper<String> wrapper = ResponseWrapper.success("test");
        StringBuilder sb = new StringBuilder();
        wrapper.onSuccess(data -> sb.append(data));

        assertEquals("test", sb.toString());
    }

    @Test
    void testOnSuccess_SkipsOnFailure() {
        ResponseWrapper<String> wrapper = ResponseWrapper.fail(new ErrorDetails("ERR", "Failed"));
        StringBuilder sb = new StringBuilder();
        wrapper.onSuccess(data -> sb.append(data));

        assertEquals("", sb.toString());
    }

    @Test
    void testOnFailure_ExecutesOnFailure() {
        ErrorDetails error = new ErrorDetails("ERR", "Failed");
        ResponseWrapper<String> wrapper = ResponseWrapper.fail(error);
        StringBuilder sb = new StringBuilder();
        wrapper.onFailure(err -> sb.append(err.errorCode()));

        assertEquals("ERR", sb.toString());
    }

    @Test
    void testOnFailure_SkipsOnSuccess() {
        ResponseWrapper<String> wrapper = ResponseWrapper.success("test");
        StringBuilder sb = new StringBuilder();
        wrapper.onFailure(err -> sb.append(err.errorCode()));

        assertEquals("", sb.toString());
    }

    @Test
    void testChainedOperations() {
        ResponseWrapper<Integer> result = ResponseWrapper.success(5)
                .map(i -> i * 2)
                .flatMap(i -> ResponseWrapper.success(i + 10))
                .map(i -> i * 3);

        assertTrue(result.isSuccess());
        assertEquals(60, result.getData());
    }
}
