package com.example.project.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class); // Добавляем логгер

    @Value("${jwt.secret}")  // Inject secret key from application.properties
    private String secretKey;

    @Value("${jwt.expiration}") // Inject expiration time from application.properties
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration}") // Inject refresh token expiration time
    private long refreshExpiration;


    public String extractUsername(String token) {
        logger.debug("Extracting username from token: " + token); // Добавляем лог
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        logger.debug("Extracting claim from token: " + token); // Добавляем лог
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
         logger.debug("Generating token for user: " + userDetails.getUsername()); // Добавляем лог
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
         logger.debug("Generating token with extra claims for user: " + userDetails.getUsername()); // Добавляем лог
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ) {
         logger.debug("Generating refresh token for user: " + userDetails.getUsername()); // Добавляем лог
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }


    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
         logger.debug("Building token for user: " + userDetails.getUsername()); // Добавляем лог
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
         logger.debug("Checking if token is valid for user: " + userDetails.getUsername()); // Добавляем лог
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
         logger.debug("Checking if token is expired: " + token); // Добавляем лог
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
         logger.debug("Extracting expiration date from token: " + token); // Добавляем лог
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
         logger.debug("Extracting all claims from token: " + token); // Добавляем лог
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
         logger.debug("Getting sign-in key"); // Добавляем лог
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}