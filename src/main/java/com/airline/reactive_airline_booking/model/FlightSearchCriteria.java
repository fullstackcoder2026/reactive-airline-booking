package com.airline.reactive_airline_booking.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FlightSearchCriteria {
    private String origin;
    private String destination;
    private LocalDate departureDate;
    private Integer passengers;
}
