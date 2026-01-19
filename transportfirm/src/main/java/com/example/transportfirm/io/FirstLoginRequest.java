package com.example.transportfirm.io;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FirstLoginRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String otp;
}
