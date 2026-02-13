package com.ebtedge.service.flow.domain.common;

public record BaseRequestMetaData(String terminalId, String uuid, String userId) {

    public static Builder builder(){
        return new Builder();
    }
    public static final class Builder{

        private String terminalId;
        private String uuid;
        private String userId;


        public Builder terminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public BaseRequestMetaData build(){
            return new BaseRequestMetaData(
                    terminalId, uuid, userId);
        }
    }
}
