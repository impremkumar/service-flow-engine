package com.ebtedge.service.flow.event.mapper;

import com.ebtedge.service.flow.domain.UIResponse;
import com.ebtedge.service.flow.event.EventMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Event mapper for UIResponse objects.
 * Selects specific fields from balance and demographics, excluding sensitive data.
 */
public enum UIResponseEventMapper implements EventMapper<UIResponse> {
    INSTANCE;

    @Override
    public Map<String, Object> map(UIResponse response) {
        Map<String, Object> payload = new HashMap<>();

        // Map Balance fields (include all fields)
        if (response.balance() != null) {
            Map<String, Object> balance = new HashMap<>();
            balance.put("clientId", response.balance().clientId());
            balance.put("amount", response.balance().amount());
            payload.put("balance", balance);
        }

        // Map Demographics fields (exclude email for privacy)
        if (response.demographics() != null) {
            Map<String, Object> demographics = new HashMap<>();
            demographics.put("name", response.demographics().name());
            demographics.put("city", response.demographics().city());
            // Deliberately exclude email for privacy
            payload.put("demographics", demographics);
        }

        return payload;
    }

    @Override
    public String schemaVersion() {
        return "1.0";
    }
}
