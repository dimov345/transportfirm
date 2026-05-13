package com.example.transportfirm.controller.auth;

import com.example.transportfirm.io.FirstLoginRequest;
import com.example.transportfirm.service.auth.FirstLoginService;
import com.example.transportfirm.service.auth.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class FirstLoginController {

    private final FirstLoginService firstLoginService;
    private final RateLimitService rateLimitService;

    @PostMapping("/first-login")
    public ResponseEntity<?> firstLogin(@Valid @RequestBody FirstLoginRequest request,
                                        HttpServletRequest httpRequest) {
        String ip = resolveClientIp(httpRequest);
        if (!rateLimitService.tryConsumeOtp(ip + ":first-login")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("status", 429, "message", "Твърде много опити. Опитайте след 10 минути."));
        }

        firstLoginService.performFirstLogin(request);
        return ResponseEntity.noContent().build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
