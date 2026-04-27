package com.example.transportfirm.controller.auth;

import com.example.transportfirm.entity.User;
import com.example.transportfirm.io.AuthRequest;
import com.example.transportfirm.io.AuthResponse;
import com.example.transportfirm.io.ResetPasswordRequest;
import com.example.transportfirm.repository.UserRepository;
import com.example.transportfirm.service.auth.AppUserDetailsService;
import com.example.transportfirm.service.auth.ProfileService;
import com.example.transportfirm.service.auth.RateLimitService;
import com.example.transportfirm.util.JwtUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        String ip = resolveClientIp(httpRequest);

        if (!rateLimitService.tryConsumeLogin(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("status", 429, "message", "Твърде много опити. Опитайте след 1 минута."));
        }

        try {
            authenticate(request.getEmail(), request.getPassword());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            if (Boolean.TRUE.equals(user.getFirstLogin())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "firstLoginRequired", true,
                                "message", "Първо влизане: трябва да смените паролата."
                        ));
            }

            var userDetails = appUserDetailsService.loadUserByUsername(request.getEmail());
            long tokenVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0L;
            String jwtToken = jwtUtil.generateToken(userDetails, tokenVersion);

            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .secure(false) // true in production (requires HTTPS)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", "Грешен email или парола."));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("status", 403, "message", "Акаунтът е деактивиран."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails,
                                    HttpServletRequest httpRequest) {
        if (userDetails != null) {
            userRepository.incrementTokenVersion(userDetails.getUsername());
        }

        ResponseCookie expired = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(Map.of("message", "Излязохте успешно."));
    }

    @PostMapping("/send-reset-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> sendResetOtp(@RequestParam String email, HttpServletRequest httpRequest) {
        String key = resolveClientIp(httpRequest) + ":" + email;

        if (!rateLimitService.tryConsumeOtp(key)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("status", 429, "message", "Твърде много заявки за OTP. Опитайте след 10 минути."));
        }

        profileService.sendResetOtp(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        profileService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
