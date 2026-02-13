package com.ebtedge.service.flow.caseinquiry;

import com.ebtedge.service.flow.core.ResponseWrapper;

public interface CaseService {

    ResponseWrapper<CaseInquiryResult> caseInquiry(CaseInquiryContext caseInquiryContext);
}
