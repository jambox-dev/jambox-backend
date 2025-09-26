package org.jambox.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests( customizers -> customizers
                                .requestMatchers(
                                        "/queue",
                                        "/completion",
                                        "/songs/search"
                                        ).permitAll()
                                .requestMatchers(
                                        "/queue/approve",
                                        "/queue/needs-approval",
                                        "/queue/needs-approval/search",
                                        "/queue/search"
                                ).permitAll()
                                .anyRequest().permitAll()
                        )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")                 // Endpoint zum Ausloggen
//                        .logoutSuccessUrl("/")               // Redirect nach Logout
//                        .invalidateHttpSession(true)        // Session löschen
//                        .deleteCookies("JSESSIONID")        // Cookie löschen
//                )
                ;

        return http.build();
    }

    // Globale CORS-Konfiguration
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Frontend-Ursprünge hier pflegen (Beispiele für Dev)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true); // nur mit expliziten Origins, nicht mit '*'

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
