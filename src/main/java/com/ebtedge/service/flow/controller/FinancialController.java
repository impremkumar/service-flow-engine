package com.ebtedge.service.flow.controller;


import com.ebtedge.service.flow.core.WorkflowPipelineFactory;
import com.ebtedge.service.flow.domain.ProfileData;
import com.ebtedge.service.flow.domain.UIResponse;
import com.ebtedge.service.flow.event.mapper.ReflectionEventMapper;
import com.ebtedge.service.flow.event.mapper.UIResponseEventMapper;
import com.ebtedge.service.flow.service.MockServiceA;
import com.ebtedge.service.flow.service.MockServiceB;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class FinancialController {

    private final MockServiceA serviceA;
    private final MockServiceB serviceB;
    private final WorkflowPipelineFactory pipelineFactory;

    @GetMapping("/{accountId}")
    public UIResponse getProfile(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9-_]{1,100}$",
                    message = "Account ID must be 1-100 characters and contain only alphanumeric, hyphens, or underscores")
            String accountId) {
        log.info("Received profile request for accountId: {}", accountId);

        UIResponse response = pipelineFactory.startWith(accountId)
                .nextStep("FetchBalance", id -> serviceA.getBalance(id))
                .peek(bal -> log.debug("Balance fetched: {}", bal))
                .nextStep("FetchDemographics", bal ->
                    serviceB.getDemographics(bal.clientId())
                        .map(demo -> new ProfileData(bal, demo)))
                .peek(profileData -> log.debug("Profile data assembled: balance={}, demographics={}",
                    profileData.balance(), profileData.demographics()))
                .mapToUI(profileData -> new UIResponse(
                    profileData.balance(),
                    profileData.demographics()))
                // Two approaches for event publishing (choose one):
                // 1. Annotation-based (RECOMMENDED) - uses @KafkaField annotations on domain classes
                .andPublishEvent("ProfileFetched", ReflectionEventMapper.INSTANCE);
                // 2. Manual mapper - provides explicit control over field selection
                // .andPublishEvent("ProfileFetched", UIResponseEventMapper.INSTANCE);

        log.info("Successfully retrieved profile for accountId: {}", accountId);
        return response;
    }
}

