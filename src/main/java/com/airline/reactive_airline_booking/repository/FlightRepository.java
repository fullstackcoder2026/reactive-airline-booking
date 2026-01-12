package com.airline.reactive_airline_booking.repository;

import com.airline.reactive_airline_booking.model.Flight;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface FlightRepository extends ReactiveMongoRepository<Flight, String> {
    Flux<Flight> findByOriginAndDestinationAndDepartureTimeBetween(
            String origin, String destination, LocalDateTime start, LocalDateTime end);
    Flux<Flight> findByAvailableSeatsGreaterThan(Integer seats);

}
