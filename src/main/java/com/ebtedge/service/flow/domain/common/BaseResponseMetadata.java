package com.ebtedge.service.flow.domain.common;

public record BaseResponseMetadata(String uuid, String responseTime) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String uuid;
        private String responseTime;

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder responseTime(String responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public BaseResponseMetadata build() {
            return new BaseResponseMetadata(uuid, responseTime);
        }
    }
}
