package org.jambox.backend.config;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.exception.UnAuthorizedException;
import org.jambox.backend.model.AuthProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthProperties authProperties;

    //todo: anständige SecConfig
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
                .sessionManagement(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/spotify")
                        .defaultSuccessUrl("/", true)              // Erfolgreiche Anmeldung Weiterleitung
                        .failureUrl("/login?error")                // Fehlgeschlagene Anmeldung
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(this.oauth2UserService())
                        )
                )
                .authorizeHttpRequests( customizers -> customizers
                                .requestMatchers(
                                        "/queue",
                                        "/completion",
                                        "/songs/search",
                                        "/login/**",
                                        "/error",
                                        "/oauth2/**",           // OAuth2-Endpunkte
                                        "/login/oauth2/**",     // OAuth2-Login-Endpunkte
                                        "/logout"
                                        ).permitAll()
                                .requestMatchers(
                                        "/queue/approve",
                                        "/queue/needs-approval",
                                        "/queue/needs-approval/search",
                                        "/queue/search"
                                ).authenticated()
                                .anyRequest().authenticated()
                        )
                .logout(logout -> logout
                        .logoutUrl("/logout")                    // URL für Logout-Anfragen
                        .logoutSuccessUrl("/")                       // Redirect nach erfolgreichem Logout
                        .deleteCookies("JSESSIONID")                 // Cookies löschen
                        .clearAuthentication(true)                   // Authentication löschen
                        .invalidateHttpSession(true)                 // Session ungültig machen
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.getWriter().write("{\"message\":\"Erfolgreich ausgeloggt\"}");
                            response.setContentType("application/json");
                        })
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("{\"error\":\"Nicht authentifiziert\"}");
                            response.setContentType("application/json");
                        })
                );


        return http.build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return userRequest -> {
            OAuth2User user = delegate.loadUser(userRequest);
            String email = user.getAttribute("email");

            if (email == null || !authProperties.getAuthorizedEmails().contains(email)) {
                throw new UnAuthorizedException("Nicht autorisierter Benutzer");
            }

            return user;
        };
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
                "http://127.0.0.1:4200",
                "https://jambox.wiegandt.tech"
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
