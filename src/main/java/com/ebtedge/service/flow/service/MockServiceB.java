package com.ebtedge.service.flow.service;


import com.ebtedge.service.flow.ResponseWrapper;
import com.ebtedge.service.flow.domain.Demographics;
import com.ebtedge.service.flow.domain.ErrorDetails;
import org.springframework.stereotype.Service;


@Service
public class MockServiceB {

    public ResponseWrapper<Demographics> getDemographics(String clientId) {
        // Simulate a failure if the client ID is missing (shouldn't happen in a happy flow)
        if (clientId == null || clientId.isEmpty()) {
            return ResponseWrapper.fail(new ErrorDetails("B_ERR_99", "Client ID is required"));
        }

        // Simulating a successful fetch
        Demographics mockDemo = new Demographics("Jane Doe", "jane.doe@example.com", "New York");
        return ResponseWrapper.success(mockDemo);
    }
}