package com.Sistema.Backend.Usuarios.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret:TextoSecretoSuperSeguroYMuyLargoParaLaFirmaDelTokenJWT2026}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMs:86400000}")
    private int jwtExpirationMs;

    // 1. GENERAR EL TOKEN JWT
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                // Pasamos directamente la clave generada por Keys para evitar el método conflictivo
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. EXTRAER EL USERNAME DEL TOKEN
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                // Usamos directamente los bytes de tu clave secreta
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 3. VALIDAR EL TOKEN
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    // Usamos directamente los bytes de tu clave secreta
                    .setSigningKey(jwtSecret.getBytes())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("El token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La cadena claims de JWT está vacía: {}", e.getMessage());
        } catch (SignatureException e) { // Cambiado de SecurityException a SignatureException por compatibilidad
            logger.error("Firma JWT inválida o no coincide: {}", e.getMessage());
        }
        return false;
    }
}