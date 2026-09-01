package dev.leo.library.infrastructure.security;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtil {

    @Value("classpath:keys/private.pem")
    private Resource privateKeyResource;

    @Value("classpath:keys/public.pem")
    private Resource publicKeyResource;

    @Value("${jwt.access-expiration:1800000}")
    private long accessExpMs;

    @Value("${jwt.refresh-expiration:86400000}")
    private long refreshExpMs;

    @Value("${jwt.issuer:library-api}")
    private String issuer;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private PrivateKey getPrivateKey() {
        if (privateKey == null) {
            try (InputStream is = privateKeyResource.getInputStream()) {
                String content = new String(is.readAllBytes())
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
                privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(content)));
            } catch (Exception e) { throw new RuntimeException("Failed to load private key", e); }
        }
        return privateKey;
    }

    private PublicKey getPublicKey() {
        if (publicKey == null) {
            try (InputStream is = publicKeyResource.getInputStream()) {
                String content = new String(is.readAllBytes())
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
                publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(content)));
            } catch (Exception e) { throw new RuntimeException("Failed to load public key", e); }
        }
        return publicKey;
    }

    public String generateAccessToken(UserEntity user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(user.getEmail()).issuer(issuer).id(UUID.randomUUID().toString())
                .claim("userId", user.getId()).claim("role", user.getRole().name())
                .claim("token_type", "access").issuedAt(new Date(now)).expiration(new Date(now + accessExpMs))
                .signWith(getPrivateKey()).compact();
    }

    public String generateRefreshToken(UserEntity user) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(user.getEmail()).issuer(issuer).id(UUID.randomUUID().toString())
                .claim("token_type", "refresh").issuedAt(new Date(now)).expiration(new Date(now + refreshExpMs))
                .signWith(getPrivateKey()).compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) { return validateToken(token).getSubject(); }
    public String extractJti(String token) { return validateToken(token).getId(); }
    public boolean isTokenExpired(String token) { return validateToken(token).getExpiration().before(new Date()); }
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
    }
}
