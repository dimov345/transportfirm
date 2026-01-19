package com.example.transportfirm.filter;

import com.example.transportfirm.service.auth.AppUserDetailsService;
import com.example.transportfirm.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
                                    FilterChain filterChain) throws ServletException, IOException {

        String servletPath = request.getServletPath();

        // Skip public endpoints
        if (PUBLIC_URLS.contains(servletPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = resolveJwt(request);

        if (jwt != null) {
            try {
                String email = jwtUtil.extractEmail(jwt);

                if (email != null && !hasRealAuthentication()) {
                    UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }

            } catch (Exception ignored) {
                // intentionally ignored (invalid token etc.)
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if ("jwt".equals(c.getName())) {
                return c.getValue();
            }
        }

        return null;
    }

    private boolean hasRealAuthentication() {
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        return existing != null
                && existing.isAuthenticated()
                && !(existing instanceof AnonymousAuthenticationToken);
    }
}
