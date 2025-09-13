package com.ejemplos.common.dto;

public record HotelReservation(
        String reservationId,
        String city,
        int nights,
        boolean confirmed,
        String reason
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String reservationId;
        private String city;
        private int nights;
        private boolean confirmed;
        private String reason;

        private Builder() {
        }

        public Builder reservationId(String reservationId) {
            this.reservationId = reservationId;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder nights(int nights) {
            this.nights = nights;
            return this;
        }

        public Builder confirmed(boolean confirmed) {
            this.confirmed = confirmed;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public HotelReservation build() {
            return new HotelReservation(reservationId, city, nights, confirmed, reason);
        }
    }
}
