package com.ejemplos.common.dto;

public record BillingCharge(
        String chargeId,
        String customerId,
        int amountCents,
        boolean charged,
        String reason
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String chargeId;
        private String customerId;
        private int amountCents;
        private boolean charged;
        private String reason;

        public Builder chargeId(String chargeId) {
            this.chargeId = chargeId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder amountCents(int amountCents) {
            this.amountCents = amountCents;
            return this;
        }

        public Builder charged(boolean charged) {
            this.charged = charged;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public BillingCharge build() {
            return new BillingCharge(chargeId, customerId, amountCents, charged, reason);
        }
    }
}
