package com.ebtedge.service.flow.caseinquiry;

import com.ebtedge.service.flow.domain.common.ClientInfoResult;

import java.util.List;

public record CaseInquiryResult(String caseNumber, List<ClientInfoResult> clients) {
}
