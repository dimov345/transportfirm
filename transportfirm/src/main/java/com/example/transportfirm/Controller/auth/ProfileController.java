package com.example.transportfirm.controller.auth;

import com.example.transportfirm.io.ChangePasswordRequest;
import com.example.transportfirm.io.ProfileResponse;
import com.example.transportfirm.service.auth.EmailService;
import com.example.transportfirm.service.auth.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    private final EmailService emailService;

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return profileService.getProfile(email);
    }

    @PostMapping("/change-password")
    public void changePassword(
            @CurrentSecurityContext(expression = "authentication?.name") String email,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(email, request);
    }

    @PostMapping("/resend-otp")
    public void resendOtp(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        profileService.resendOtp(email);
    }



}
