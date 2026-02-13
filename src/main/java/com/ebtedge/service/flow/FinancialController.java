package com.ebtedge.service.flow;


import com.ebtedge.service.flow.domain.Balance;
import com.ebtedge.service.flow.domain.Demographics;
import com.ebtedge.service.flow.domain.UIResponse;
import com.ebtedge.service.flow.service.MockServiceA;
import com.ebtedge.service.flow.service.MockServiceB;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class FinancialController {

    private final MockServiceA serviceA;
    private final MockServiceB serviceB;

    @GetMapping("/{accountId}")
    public UIResponse getProfile(@PathVariable String accountId) {
        Map<String, Object> context = new HashMap<>();

        return WorkflowPipeline.startWith(accountId)
                .nextStep("FetchBalance", id -> serviceA.getBalance(id))
                .peek(bal -> context.put("BAL", bal))
                .nextStep("FetchDemographics", bal -> serviceB.getDemographics(bal.clientId()))
                .peek(demo -> context.put("DEMO", demo))
                .mapToUI(last -> new UIResponse(
                        (Balance) context.get("BAL"),
                        (Demographics) context.get("DEMO")
                ));
    }
}

