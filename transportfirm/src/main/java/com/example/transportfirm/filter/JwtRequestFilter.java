package com.example.transportfirm.filter;

import com.example.transportfirm.service.auth.AppUserDetailsService;
import com.example.transportfirm.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_URLS = List.of(
            "/login",
            "/register",
            "/send-reset-otp",
            "/reset-password",
            "/logout",
            "/auth/first-login"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        // 1️⃣ Пропускаме public endpoints
        if (PUBLIC_URLS.contains(servletPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2️⃣ Вземаме JWT (header или cookie)
            String jwt = resolveJwt(request);

            if (jwt != null) {
                // 3️⃣ Извличаме email (subject)
                String email = jwtUtil.extractEmail(jwt);

                if (email != null) {
                    // 4️⃣ Зареждаме user-а
                    UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);

                    // 5️⃣ Валидираме token-а
                    if (jwtUtil.validateToken(jwt, userDetails)) {

                        // 6️⃣ Създаваме Authentication
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // 7️⃣ ВИНАГИ сетваме SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("JWT authentication failed for request [{}]: {}", request.getServletPath(), ex.getMessage());
        }

        // 8️⃣ Продължаваме по веригата
        filterChain.doFilter(request, response);
    }

    private String resolveJwt(HttpServletRequest request) {
        // 1) Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2) Cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if ("jwt".equals(c.getName())) {
                return c.getValue();
            }
        }

        return null;
    }
}
