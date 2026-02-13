package com.example.transportfirm.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException ex) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write("""
      {"status":403,"error":"FORBIDDEN","message":"Access denied"}
    """);
    }
}
