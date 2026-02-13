package com.ebtedge.service.flow.domain;

public record CardInfo(String cardNumber, String cardPrefix, String cardStatus,
                       String cardStatusDisplay, String cardFee, String cardAge, String lastUpdatedTs) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String cardNumber;
        private String cardPrefix;
        private String cardStatus;
        private String cardStatusDisplay;
        private String cardFee;
        private String cardAge;
        private String lastUpdatedTs;

        public Builder cardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
            return this;
        }

        public Builder cardPrefix(String cardPrefix) {
            this.cardPrefix = cardPrefix;
            return this;
        }

        public Builder cardStatus(String cardStatus) {
            this.cardStatus = cardStatus;
            return this;
        }

        public Builder cardStatusDisplay(String cardStatusDisplay) {
            this.cardStatusDisplay = cardStatusDisplay;
            return this;
        }

        public Builder cardFee(String cardFee) {
            this.cardFee = cardFee;
            return this;
        }

        public Builder cardAge(String cardAge) {
            this.cardAge = cardAge;
            return this;
        }

        public Builder lastUpdatedTs(String lastUpdatedTs) {
            this.lastUpdatedTs = lastUpdatedTs;
            return this;
        }

        public CardInfo build() {
            return new CardInfo(cardNumber, cardPrefix, cardStatus, cardStatusDisplay,
                              cardFee, cardAge, lastUpdatedTs);
        }
    }
}
