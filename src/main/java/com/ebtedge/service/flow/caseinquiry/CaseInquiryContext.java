package com.ebtedge.service.flow.caseinquiry;

import com.ebtedge.service.flow.domain.common.BaseRequestMetaData;

public record CaseInquiryContext(BaseRequestMetaData baseRequest, String caseNumber, String agency) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private BaseRequestMetaData baseRequest;
        private String caseNumber;
        private String agency;

        public Builder baseRequest(BaseRequestMetaData baseRequest) {
            this.baseRequest = baseRequest;
            return this;
        }

        public Builder caseNumber(String caseNumber) {
            this.caseNumber = caseNumber;
            return this;
        }

        public Builder agency(String agency) {
            this.agency = agency;
            return this;
        }

        public CaseInquiryContext build() {
            return new CaseInquiryContext(baseRequest, caseNumber, agency);
        }
    }
}
