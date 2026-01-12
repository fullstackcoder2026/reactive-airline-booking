package com.airline.reactive_airline_booking.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Booking {

    @Id
    private String id;
    private String flightId;
    private String passengerId;
    private String passengerName;
    private String passengerEmail;
    private String seatNumber;
    private Double price;
    private BookingStatus status;
    private LocalDateTime bookingTime;
    private String confirmationCode;

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }
}
