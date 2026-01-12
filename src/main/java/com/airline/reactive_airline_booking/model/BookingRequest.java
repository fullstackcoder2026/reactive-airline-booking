package com.airline.reactive_airline_booking.model;

import lombok.Data;

@Data
public class BookingRequest {
    private String flightId;
    private String passengerId;
    private String passengerName;
    private String passengerEmail;
    private String seatPreference;
}
