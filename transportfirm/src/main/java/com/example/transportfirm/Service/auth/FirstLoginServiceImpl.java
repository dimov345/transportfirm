package com.example.transportfirm.service.auth;

import com.example.transportfirm.entity.User;
import com.example.transportfirm.io.FirstLoginRequest;
import com.example.transportfirm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.BadCredentialsException;

@Service
@RequiredArgsConstructor
public class FirstLoginServiceImpl implements FirstLoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void performFirstLogin(FirstLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getFirstLogin()) {
            throw new RuntimeException("First login already completed");
        }


        if (!request.getOtp().equals(user.getVerifyOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        user.setIsAccountVerified(true);
        user.setVerifyOtp(null);
        user.setVerifyOtpExpireAt(0L);

        userRepository.save(user);
    }

}