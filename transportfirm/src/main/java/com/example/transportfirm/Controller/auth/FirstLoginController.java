package com.example.transportfirm.controller.auth;

import com.example.transportfirm.io.FirstLoginRequest;
import com.example.transportfirm.service.auth.FirstLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class FirstLoginController {

    private final FirstLoginService firstLoginService;

    @PostMapping("/first-login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void firstLogin(@Valid @RequestBody FirstLoginRequest request) {
        firstLoginService.performFirstLogin(request);
    }
}
