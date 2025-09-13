package com.ejemplos.common.dto;

public record FlightReservation(
        String reservationId,
        String fromAirport,
        String toAirport,
        String date, // ISO-8601
        boolean confirmed,
        String reason
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String reservationId;
        private String fromAirport;
        private String toAirport;
        private String date;
        private boolean confirmed;
        private String reason;

        public Builder reservationId(String reservationId) {
            this.reservationId = reservationId;
            return this;
        }

        public Builder fromAirport(String fromAirport) {
            this.fromAirport = fromAirport;
            return this;
        }

        public Builder toAirport(String toAirport) {
            this.toAirport = toAirport;
            return this;
        }

        public Builder date(String date) {
            this.date = date;
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

        public FlightReservation build() {
            return new FlightReservation(reservationId, fromAirport, toAirport, date, confirmed, reason);
        }
    }
}
