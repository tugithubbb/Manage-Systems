package com.example.demo.config;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {

            String token = auth.substring(7);

            try {
                SignedJWT jwt = SignedJWT.parse(token);

                JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

                if (jwt.verify(verifier)) {
                    String username = jwt.getJWTClaimsSet().getSubject();
                    String role = (String) jwt.getJWTClaimsSet().getClaim("role");
                    List<String> permissions =
                            (List<String>) jwt.getJWTClaimsSet().getClaim("permissions");

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    if (role != null) authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    if (permissions != null) permissions.forEach(p ->
                            authorities.add(new SimpleGrantedAuthority(p)));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception ignored) {}
        }

        filterChain.doFilter(request, response);
    }
}
