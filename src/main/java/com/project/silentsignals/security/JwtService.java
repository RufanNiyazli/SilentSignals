package com.project.silentsignals.security;

import com.project.silentsignals.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRoleType());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .expiration(new Date(System.currentTimeMillis() * 1000 * 60 * 10))
                .issuedAt(new Date())
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();

    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String getEmail(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails user) {
        final String username = getEmail(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    public String getRoleByToken(String token) {
        return extractClaims(token, claims -> claims.get("role", String.class));
    }


}
