package com.ebtedge.service.flow.service;


import com.ebtedge.service.flow.core.ResponseWrapper;
import com.ebtedge.service.flow.domain.Balance;
import com.ebtedge.service.flow.domain.ErrorDetails;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MockServiceA {

    public ResponseWrapper<Balance> getBalance(String accountId) {
        log.info("MockServiceA: Fetching balance for accountId: {}", accountId);

        // Simple validation logic to simulate a real service failure
        if ("invalid".equalsIgnoreCase(accountId)) {
            log.warn("MockServiceA: Invalid accountId detected: {}", accountId);
            return ResponseWrapper.fail(new ErrorDetails("A_ERR_01", "Account not found in Ledger"));
        }

        // Simulating a successful fetch
        Balance mockBalance = new Balance("CUST-7788", 1250.50);
        log.debug("MockServiceA: Successfully fetched balance: {}", mockBalance);
        return ResponseWrapper.success(mockBalance);
    }
}