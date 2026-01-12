package com.airline.reactive_airline_booking.service;

import com.airline.reactive_airline_booking.model.Booking;
import com.airline.reactive_airline_booking.model.BookingRequest;
import com.airline.reactive_airline_booking.model.Flight;
import com.airline.reactive_airline_booking.repository.BookingRepository;
import com.airline.reactive_airline_booking.repository.FlightRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final PricingService pricingService;
    private final SeatAllocationService seatAllocationService;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository,
                          FlightRepository flightRepository,
                          PricingService pricingService,
                          SeatAllocationService seatAllocationService,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.pricingService = pricingService;
        this.seatAllocationService = seatAllocationService;
        this.notificationService = notificationService;
    }

    // Complex reactive booking flow with multiple async operations
    public Mono<Booking> createBooking(BookingRequest request) {
        return flightRepository.findById(request.getFlightId())
                .switchIfEmpty(Mono.error(new RuntimeException("Flight not found")))
                .filter(flight -> flight.getAvailableSeats() > 0)
                .switchIfEmpty(Mono.error(new RuntimeException("No seats available")))
                .flatMap(flight ->
                        Mono.zip(
                                        pricingService.calculatePrice(flight),
                                        seatAllocationService.allocateSeat(flight.getId(), request.getSeatPreference())
                                )
                                .flatMap(tuple -> {
                                    Double price = tuple.getT1();
                                    String seatNumber = tuple.getT2();

                                    Booking booking = new Booking(
                                            flight.getId(),
                                            request.getPassengerId(),
                                            request.getPassengerName(),
                                            request.getPassengerEmail(),
                                            seatNumber,
                                            price
                                    );
                                    booking.setConfirmationCode(generateConfirmationCode());
                                    booking.setStatus(Booking.BookingStatus.CONFIRMED);

                                    return bookingRepository.save(booking)
                                            .flatMap(savedBooking ->
                                                    updateFlightSeats(flight.getId(), -1)
                                                            .thenReturn(savedBooking)
                                            )
                                            .doOnSuccess(notificationService::sendBookingConfirmation);
                                })
                );
    }

    public Mono<Booking> cancelBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .switchIfEmpty(Mono.error(new RuntimeException("Booking not found")))
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.CONFIRMED)
                .switchIfEmpty(Mono.error(new RuntimeException("Booking cannot be cancelled")))
                .flatMap(booking -> {
                    booking.setStatus(Booking.BookingStatus.CANCELLED);
                    return bookingRepository.save(booking)
                            .flatMap(cancelled ->
                                    updateFlightSeats(cancelled.getFlightId(), 1)
                                            .thenReturn(cancelled)
                            );
                });
    }

    public Flux<Booking> getPassengerBookings(String passengerId) {
        return bookingRepository.findByPassengerId(passengerId);
    }

    public Mono<Booking> getBookingByConfirmation(String confirmationCode) {
        return bookingRepository.findByConfirmationCode(confirmationCode);
    }

    private Mono<Flight> updateFlightSeats(String flightId, int delta) {
        return flightRepository.findById(flightId)
                .flatMap(flight -> {
                    flight.setAvailableSeats(flight.getAvailableSeats() + delta);
                    return flightRepository.save(flight);
                });
    }

    private String generateConfirmationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


}
