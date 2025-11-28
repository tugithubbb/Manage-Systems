package com.example.demo.config;

import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @Bean
    public JwtDecoder jwtDecoder() {
        // Chuyển key sang byte[]
        byte[] keyBytes = SIGNER_KEY.getBytes(StandardCharsets.UTF_8);
        // Tạo SecretKey với HmacSHA512
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");

        // Trả về decoder dùng HS512
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}
