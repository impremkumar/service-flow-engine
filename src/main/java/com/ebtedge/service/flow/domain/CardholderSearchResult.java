package com.ebtedge.service.flow.domain;

import com.ebtedge.service.flow.domain.common.BaseResponseMetadata;

import java.util.List;

public record CardholderSearchResult(BaseResponseMetadata baseResponse, List<Cardholder> cardholderList, String offset) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private BaseResponseMetadata baseResponse;
        private List<Cardholder> cardholderList;
        private String offset;

        public Builder baseResponse(BaseResponseMetadata baseResponse) {
            this.baseResponse = baseResponse;
            return this;
        }

        public Builder cardholderList(List<Cardholder> cardholderList) {
            this.cardholderList = cardholderList;
            return this;
        }

        public Builder offset(String offset) {
            this.offset = offset;
            return this;
        }

        public CardholderSearchResult build() {
            return new CardholderSearchResult(baseResponse, cardholderList, offset);
        }
    }
}
