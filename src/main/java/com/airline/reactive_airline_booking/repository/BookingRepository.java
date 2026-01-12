package com.airline.reactive_airline_booking.repository;

import com.airline.reactive_airline_booking.model.Booking;
import com.airline.reactive_airline_booking.model.Flight;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface BookingRepository extends ReactiveMongoRepository<Flight, String> {
    Flux<Booking> findByPassengerId(String passengerId);
    Flux<Booking> findByFlightId(String flightId);
    Mono<Booking> findByConfirmationCode(String confirmationCode);

}
