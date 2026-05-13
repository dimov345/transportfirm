package com.example.transportfirm.service.auth;

import com.example.transportfirm.io.auth.ChangePasswordRequest;
import com.example.transportfirm.io.auth.ProfileRequest;
import com.example.transportfirm.io.auth.ProfileResponse;

public interface ProfileService {

    ProfileResponse getProfile(String email);

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);

    void changePassword(String email, ChangePasswordRequest request);

    void resendOtp(String email);

}
