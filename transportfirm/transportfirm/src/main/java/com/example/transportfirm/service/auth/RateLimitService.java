package com.example.transportfirm.service.auth;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter per IP / key.
 * Login: max 10 attempts per minute.
 * OTP:   max 5 requests per 10 minutes.
 */
@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> otpBuckets   = new ConcurrentHashMap<>();

    public boolean tryConsumeLogin(String ip) {
        return loginBuckets.computeIfAbsent(ip, k -> buildLoginBucket()).tryConsume(1);
    }

    public boolean tryConsumeOtp(String key) {
        return otpBuckets.computeIfAbsent(key, k -> buildOtpBucket()).tryConsume(1);
    }

    private Bucket buildLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofMinutes(1)))
                .build();
    }

    private Bucket buildOtpBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofMinutes(10)))
                .build();
    }
}
