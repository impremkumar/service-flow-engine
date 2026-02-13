package com.ebtedge.service.flow.service;


import com.ebtedge.service.flow.ResponseWrapper;
import com.ebtedge.service.flow.domain.Balance;
import com.ebtedge.service.flow.domain.ErrorDetails;
import org.springframework.stereotype.Service;

@Service
public class MockServiceA {

    public ResponseWrapper<Balance> getBalance(String accountId) {
        // Simple validation logic to simulate a real service failure
        if ("invalid".equalsIgnoreCase(accountId)) {
            return ResponseWrapper.fail(new ErrorDetails("A_ERR_01", "Account not found in Ledger"));
        }

        // Simulating a successful fetch
        Balance mockBalance = new Balance("CUST-7788", 1250.50);
        return ResponseWrapper.success(mockBalance);
    }
}