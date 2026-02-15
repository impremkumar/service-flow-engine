package com.ebtedge.service.flow.event.mapper;

import com.ebtedge.service.flow.domain.Balance;
import com.ebtedge.service.flow.event.EventMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Event mapper for Balance objects.
 * Maps all balance fields to the Kafka event payload.
 */
public enum BalanceEventMapper implements EventMapper<Balance> {
    INSTANCE;

    @Override
    public Map<String, Object> map(Balance balance) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("clientId", balance.clientId());
        payload.put("amount", balance.amount());
        return payload;
    }

    @Override
    public String schemaVersion() {
        return "1.0";
    }
}
