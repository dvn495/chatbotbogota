package com.example.chatbotbogota.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.time.Duration;  // Importación de Duration

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Se recomienda manejar esta clave de forma segura (no hardcodeada en el código).
    private static final String SECRET_KEY = "586E3272357538782F413F4428472B4B6250655368566B597033733676397924";

    // Método para obtener el token
    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }

    // Método privado para crear un token con las reclamaciones adicionales
    private String getToken(Map<String, Object> extraClaims, UserDetails user) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + Duration.ofDays(1).toMillis())) // Expira en 24 horas
                .signWith(getKey(), SignatureAlgorithm.HS256) // Firma con la clave secreta
                .compact();
    }

    // Método para obtener la clave secreta
    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); // Decodificación BASE64
        return Keys.hmacShaKeyFor(keyBytes); // Generar clave HMAC
    }

    // Método para obtener el nombre de usuario desde el token
    public String getUsernameFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    // Verifica si el token es válido comparando con los detalles del usuario
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Verifica si el token es válido solo con base en la fecha de expiración
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Agrega log aquí para diagnóstico
            return false;
        }
    }

    // Obtiene todas las reclamaciones del token
    private Claims getAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("Received an empty or null token.");
            throw new IllegalArgumentException("Token is null or empty");
        }

        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException e) {
            System.err.println("Malformed JWT token: " + token);
            throw e;
        }
    }

    // Método para obtener un valor específico de las reclamaciones
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Obtiene la fecha de expiración del token
    private Date getExpiration(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    // Verifica si el token ha expirado
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
