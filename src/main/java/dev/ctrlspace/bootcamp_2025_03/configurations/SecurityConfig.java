package dev.ctrlspace.bootcamp_2025_03.configurations;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import dev.ctrlspace.bootcamp_2025_03.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // allow all origins
        config.setAllowedOriginPatterns(List.of("*"));
        // allow all standard HTTP methods
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        // allow any header
        config.setAllowedHeaders(List.of("*"));
        // if you need to expose any response headers to the browser
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        // if you want to allow cookies/credentials
        config.setAllowCredentials(true);

        // apply this config to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService) throws Exception {
        http
                // all endpoints require authentication
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers("/login", "/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/password-reset-request").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/password-reset").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/password-reset").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/verify").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/users/{id}/change-password").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/{id}/profile-settings").authenticated()
                        .anyRequest().authenticated()
                )
                .userDetailsService(userService)
                // enable HTTP Basic
                .httpBasic(Customizer.withDefaults())
                // replace the deprecated jwt() reference with:
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                // disable CSRF if you’re only serving a pure REST API
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
         return NoOpPasswordEncoder.getInstance();
    }




    // 1) Create an RSA JWK (public+private) for signing
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws JOSEException {
        RSAKey rsaJwk = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("rsa-key-1")
                .generate();

        JWKSet jwkSet = new JWKSet(rsaJwk);
        return new ImmutableJWKSet<>(jwkSet);
    }

    // 2) Expose a JwtEncoder that will sign tokens with our JWK
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) throws JOSEException {
        // 1) select the JWK with your key-ID
        JWKSelector selector = new JWKSelector(
                new JWKMatcher.Builder()
                        .keyID("rsa-key-1")
                        .keyUse(KeyUse.SIGNATURE)
                        .build()
        );
        // 2) query the source
        List<JWK> jwks = jwkSource.get(selector, null);
        if (jwks.isEmpty()) {
            throw new IllegalStateException("No RSA JWK found with keyID rsa-key-1");
        }
        RSAKey rsa = (RSAKey) jwks.get(0);

        // 3) extract the Java PublicKey
        RSAPublicKey publicKey = rsa.toRSAPublicKey();

        // 4) plug into the Nimbus decoder
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }



}
