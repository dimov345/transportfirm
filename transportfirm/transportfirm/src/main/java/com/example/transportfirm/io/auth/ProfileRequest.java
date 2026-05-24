package com.example.transportfirm.io.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileRequest {

    @Email(message = "Enter valid email address")
    @NotBlank(message = "Email should not be empty")
    private String email;

    @Size(min = 6, message = "Password should be at least 6 symbols")
    private String password;
}
