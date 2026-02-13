package com.ebtedge.service.flow.service;


import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.Demographics;
import com.ebtedge.service.flow.domain.ErrorDetails;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class MockServiceB {

    public ResponseWrapper<Demographics> getDemographics(String clientId) {
        log.info("MockServiceB: Fetching demographics for clientId: {}", clientId);

        // Simulate a failure if the client ID is missing (shouldn't happen in a happy flow)
        if (clientId == null || clientId.isEmpty()) {
            log.warn("MockServiceB: Missing or empty clientId");
            return ResponseWrapper.fail(new ErrorDetails("B_ERR_99", "Client ID is required"));
        }

        // Simulating a successful fetch
        Demographics mockDemo = new Demographics("Jane Doe", "jane.doe@example.com", "New York");
        log.debug("MockServiceB: Successfully fetched demographics: {}", mockDemo);
        return ResponseWrapper.success(mockDemo);
    }
}