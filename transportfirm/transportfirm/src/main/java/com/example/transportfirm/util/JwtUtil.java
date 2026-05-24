package com.example.transportfirm.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key:}")
    private String SECRET_KEY;

    private byte[] signingKeyBytes() {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            throw new IllegalStateException("JWT secret key is missing. Set env var JWT_SECRET.");
        }
        return hexToBytes(SECRET_KEY.trim());
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, 0L);
    }

    public String generateToken(UserDetails userDetails, long tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        claims.put("tv", tokenVersion);
        return createToken(claims, userDetails.getUsername());
    }

    public long extractTokenVersion(String token) {
        Object tv = extractAllClaims(token).get("tv");
        if (tv instanceof Number n) return n.longValue();
        return 0L;
    }

    private String createToken(Map<String, Object> claims, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000L * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, signingKeyBytes())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(signingKeyBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email != null && email.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private static byte[] hexToBytes(String hex) {
        String s = hex.replace("0x", "").replace(" ", "");
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException("JWT_SECRET must be even-length hex string.");
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
