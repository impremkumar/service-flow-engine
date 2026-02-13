package com.ebtedge.service.flow.cardsummary;

import com.ebtedge.service.flow.domain.common.BaseRequestMetaData;

public record CardholderSummaryContext(BaseRequestMetaData baseRequest, String cardNumber, String agency) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private BaseRequestMetaData baseRequest;
        private String cardNumber;
        private String agency;

        public Builder baseRequest(BaseRequestMetaData baseRequest) {
            this.baseRequest = baseRequest;
            return this;
        }

        public Builder cardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder agency(String agency) {
            this.agency = agency;
            return this;
        }

        public CardholderSummaryContext build() {
            return new CardholderSummaryContext(baseRequest, cardNumber, agency);
        }
    }
}
