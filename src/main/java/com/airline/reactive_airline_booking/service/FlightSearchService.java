package com.airline.reactive_airline_booking.service;

import com.airline.reactive_airline_booking.model.Flight;
import com.airline.reactive_airline_booking.model.FlightSearchCriteria;
import com.airline.reactive_airline_booking.repository.FlightRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class FlightSearchService {

    private final FlightRepository flightRepository;
    private final PricingService pricingService;

    public FlightSearchService(FlightRepository flightRepository, PricingService pricingService) {
        this.flightRepository = flightRepository;
        this.pricingService = pricingService;
    }

    // Search with real-time price calculation
    public Flux<Flight> searchFlights(FlightSearchCriteria criteria) {
        LocalDateTime startOfDay = criteria.getDepartureDate().atStartOfDay();
        LocalDateTime endOfDay = criteria.getDepartureDate().atTime(LocalTime.MAX);

        return flightRepository.findByOriginAndDestinationAndDepartureTimeBetween(
                        criteria.getOrigin(),
                        criteria.getDestination(),
                        startOfDay,
                        endOfDay)
                .filter(flight -> flight.getAvailableSeats() >= criteria.getPassengers())
                .flatMap(flight ->
                        pricingService.calculatePrice(flight)
                                .map(price -> {
                                    flight.setBasePrice(price); // Update with current price
                                    return flight;
                                })
                )
                .sort((f1, f2) -> f1.getBasePrice().compareTo(f2.getBasePrice()));
    }

    public Flux<Flight> getAvailableFlights() {
        return flightRepository.findByAvailableSeatsGreaterThan(0)
                .filter(flight -> flight.getDepartureTime().isAfter(LocalDateTime.now()));
    }
}
