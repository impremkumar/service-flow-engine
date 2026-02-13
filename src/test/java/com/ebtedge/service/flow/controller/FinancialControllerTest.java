package com.ebtedge.service.flow.controller;

import com.ebtedge.service.flow.autoconfigure.ServiceFlowProperties;
import com.ebtedge.service.flow.core.WorkflowPipelineFactory;
import com.ebtedge.service.flow.exception.GlobalHandler;
import com.ebtedge.service.flow.service.MockServiceA;
import com.ebtedge.service.flow.service.MockServiceB;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialController.class)
@Import({MockServiceA.class, MockServiceB.class, GlobalHandler.class})
@ContextConfiguration(classes = {FinancialController.class, MockServiceA.class, MockServiceB.class, GlobalHandler.class, FinancialControllerTest.TestConfig.class})
class FinancialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    static class TestConfig {
        @Bean
        public ServiceFlowProperties serviceFlowProperties() {
            return new ServiceFlowProperties();
        }

        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        public WorkflowPipelineFactory workflowPipelineFactory(ServiceFlowProperties properties, MeterRegistry meterRegistry) {
            return new WorkflowPipelineFactory(properties, meterRegistry);
        }
    }

    @Test
    void testGetProfile_Success() throws Exception {
        mockMvc.perform(get("/api/profile/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.balance.clientId").value("CUST-7788"))
                .andExpect(jsonPath("$.balance.amount").value(1250.50))
                .andExpect(jsonPath("$.demographics").exists())
                .andExpect(jsonPath("$.demographics.name").value("Jane Doe"))
                .andExpect(jsonPath("$.demographics.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.demographics.city").value("New York"));
    }

    @Test
    void testGetProfile_InvalidAccountId() throws Exception {
        mockMvc.perform(get("/api/profile/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("A_ERR_01"))
                .andExpect(jsonPath("$.message").value("Account not found in Ledger"));
    }

    @Test
    void testGetProfile_DifferentAccountId() throws Exception {
        mockMvc.perform(get("/api/profile/ACC-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.clientId").value("CUST-7788"));
    }

    @Test
    void testGetProfile_InvalidUppercase() throws Exception {
        mockMvc.perform(get("/api/profile/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    void testGetProfile_ValidAccount_VerifyAllFields() throws Exception {
        mockMvc.perform(get("/api/profile/TEST-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").isNotEmpty())
                .andExpect(jsonPath("$.demographics").isNotEmpty())
                .andExpect(jsonPath("$.balance.clientId").isString())
                .andExpect(jsonPath("$.balance.amount").isNumber())
                .andExpect(jsonPath("$.demographics.name").isString())
                .andExpect(jsonPath("$.demographics.email").isString())
                .andExpect(jsonPath("$.demographics.city").isString());
    }

    @Test
    void testGetProfile_NumericAccountId() throws Exception {
        mockMvc.perform(get("/api/profile/999888777"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance.amount").value(1250.50));
    }

    @Test
    void testGetProfile_ValidationFailure_SpecialCharacters() throws Exception {
        mockMvc.perform(get("/api/profile/test@#$"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetProfile_ValidationFailure_TooLong() throws Exception {
        String longId = "a".repeat(101);
        mockMvc.perform(get("/api/profile/" + longId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetProfile_ValidationSuccess_Hyphens() throws Exception {
        mockMvc.perform(get("/api/profile/ACC-123-456"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProfile_ValidationSuccess_Underscores() throws Exception {
        mockMvc.perform(get("/api/profile/ACC_123_456"))
                .andExpect(status().isOk());
    }
}
