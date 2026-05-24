package com.example.transportfirm.service.auth;

import com.example.transportfirm.io.auth.FirstLoginRequest;

public interface FirstLoginService {
    void performFirstLogin( FirstLoginRequest request);
}