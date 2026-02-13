package com.ebtedge.service.flow.domain;

public record ClientInfo(String clientId, String caseNumber, String firstName, String lastName, String dateOfBirth) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String clientId;
        private String caseNumber;
        private String firstName;
        private String lastName;
        private String dateOfBirth;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder caseNumber(String caseNumber) {
            this.caseNumber = caseNumber;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder dateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public ClientInfo build() {
            return new ClientInfo(clientId, caseNumber, firstName, lastName, dateOfBirth);
        }
    }
}
