package com.airline.reactive_airline_booking.service;

import com.airline.reactive_airline_booking.model.Booking;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Service
public class NotificationService {

    private final Sinks.Many<String> notificationSink = Sinks.many().multicast().onBackpressureBuffer();

    public void sendBookingConfirmation(Booking booking) {
        String message = String.format("Booking confirmed for %s - Flight: %s, Seat: %s, Confirmation: %s",
                booking.getPassengerName(), booking.getFlightId(),
                booking.getSeatNumber(), booking.getConfirmationCode());

        notificationSink.tryEmitNext(message);

        // Simulate email sending
        System.out.println("Email sent to: " + booking.getPassengerEmail());
    }

    // Server-Sent Events stream for real-time notifications
    public Flux<String> getNotificationStream() {
        return notificationSink.asFlux()
                .delayElements(Duration.ofMillis(100));
    }
}
