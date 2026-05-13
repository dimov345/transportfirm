package com.example.transportfirm.filter;

import com.example.transportfirm.entity.User;
import com.example.transportfirm.repository.auth.UserRepository;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final AppUserDetailsService appUserDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_URLS = List.of(
            "/login",
            "/register",
            "/send-reset-otp",
            "/reset-password",
            "/logout",
            "/auth/first-login",
            "/auth/bootstrap"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        if (PUBLIC_URLS.stream().anyMatch(servletPath::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = resolveJwt(request);

            if (jwt != null) {
                String email = jwtUtil.extractEmail(jwt);

                if (email != null) {
                    UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        // Token version check — rejects tokens issued before last logout
                        long jwtVersion = jwtUtil.extractTokenVersion(jwt);
                        Optional<User> userOpt = userRepository.findByEmailWithEmployee(email);

                        if (userOpt.isPresent() && userOpt.get().getTokenVersion() == jwtVersion) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities()
                                    );
                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            log.warn("Rejected revoked token for user [{}]", email);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("JWT authentication failed for [{}]: {}", request.getServletPath(), ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveJwt(HttpServletRequest request) {
        // Authorization header (kept for backward compat with Swagger/tools)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // httpOnly cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("jwt".equals(c.getName())) return c.getValue();
        }

        return null;
    }
}
