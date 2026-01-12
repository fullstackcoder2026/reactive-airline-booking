package com.airline.reactive_airline_booking.service;

import com.airline.reactive_airline_booking.model.Flight;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PricingService {
    // Dynamic pricing based on seat availability and time to departure
    public Mono<Double> calculatePrice(Flight flight) {
        return Mono.fromCallable(() -> {
            Double basePrice = flight.getBasePrice();

            // Demand-based pricing
            double occupancyRate = 1.0 - ((double) flight.getAvailableSeats() / flight.getTotalSeats());
            double demandMultiplier = 1.0 + (occupancyRate * 0.5); // Up to 50% increase

            // Time-based pricing (closer to departure = higher price)
            long hoursUntilDeparture = Duration.between(LocalDateTime.now(),
                    flight.getDepartureTime()).toHours();
            double urgencyMultiplier = hoursUntilDeparture < 24 ? 1.3 :
                    hoursUntilDeparture < 72 ? 1.15 : 1.0;

            return basePrice * demandMultiplier * urgencyMultiplier;
        });
    }
}
