package com.airline.reactive_airline_booking.service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class SeatAllocationService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public SeatAllocationService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Allocate seat with Redis-based locking for concurrency control
    public Mono<String> allocateSeat(String flightId, String preference) {
        String lockKey = "flight:lock:" + flightId;

        return redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", java.time.Duration.ofSeconds(5))
                .flatMap(acquired -> {
                    if (Boolean.TRUE.equals(acquired)) {
                        return generateSeatNumber(flightId, preference)
                                .flatMap(seat -> markSeatAsOccupied(flightId, seat)
                                        .thenReturn(seat))
                                .doFinally(signal -> releaseLock(lockKey).subscribe());
                    } else {
                        return Mono.error(new RuntimeException("Could not acquire lock"));
                    }
                });
    }

    private Mono<String> generateSeatNumber(String flightId, String preference) {
        // Simplified seat generation
        int row = ThreadLocalRandom.current().nextInt(1, 31);
        char seatLetter = switch (preference != null ? preference : "AISLE") {
            case "WINDOW" -> ThreadLocalRandom.current().nextBoolean() ? 'A' : 'F';
            case "MIDDLE" -> ThreadLocalRandom.current().nextBoolean() ? 'B' : 'E';
            default -> ThreadLocalRandom.current().nextBoolean() ? 'C' : 'D';
        };
        return Mono.just(row + String.valueOf(seatLetter));
    }

    private Mono<Boolean> markSeatAsOccupied(String flightId, String seat) {
        return redisTemplate.opsForSet()
                .add("flight:seats:" + flightId, seat)
                .map(result -> result > 0);
    }

    private Mono<Boolean> releaseLock(String lockKey) {
        return redisTemplate.delete(lockKey).map(result -> result > 0);
    }
}
