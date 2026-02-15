package com.ebtedge.service.flow.event.mapper;

import com.ebtedge.service.flow.domain.Demographics;
import com.ebtedge.service.flow.event.EventMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Event mapper for Demographics objects.
 * Maps selected demographics fields, excluding sensitive information like email.
 */
public enum DemographicsEventMapper implements EventMapper<Demographics> {
    INSTANCE;

    @Override
    public Map<String, Object> map(Demographics demographics) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", demographics.name());
        payload.put("city", demographics.city());
        // Deliberately exclude email for privacy
        return payload;
    }

    @Override
    public String schemaVersion() {
        return "1.0";
    }
}
