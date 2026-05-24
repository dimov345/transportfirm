package com.example.transportfirm.controller.auth;

import com.example.transportfirm.io.auth.ChangePasswordRequest;
import com.example.transportfirm.io.auth.ProfileResponse;
import com.example.transportfirm.service.auth.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return profileService.getProfile(email);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @CurrentSecurityContext(expression = "authentication?.name") String email,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(email, request);
    }

    @PostMapping("/resend-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendOtp(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        profileService.resendOtp(email);
    }
}
