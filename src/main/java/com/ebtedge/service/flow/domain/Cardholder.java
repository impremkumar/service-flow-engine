package com.ebtedge.service.flow.domain;

public record Cardholder(String cardholderId, String score, ClientInfo clientInfo, CardInfo cardInfo) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String cardholderId;
        private String score;
        private ClientInfo clientInfo;
        private CardInfo cardInfo;

        public Builder cardholderId(String cardholderId) {
            this.cardholderId = cardholderId;
            return this;
        }

        public Builder score(String score) {
            this.score = score;
            return this;
        }

        public Builder clientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public Builder cardInfo(CardInfo cardInfo) {
            this.cardInfo = cardInfo;
            return this;
        }

        public Cardholder build() {
            return new Cardholder(cardholderId, score, clientInfo, cardInfo);
        }
    }
}
