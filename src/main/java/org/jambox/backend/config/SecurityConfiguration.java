package org.jambox.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jambox.backend.exception.UnAuthorizedException;
import org.jambox.backend.model.AuthProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final TenantFilter tenantFilter;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final AuthProperties authProperties;
    @Value("${jambox.oauth2-login-success-redirect}")
    private String oauth2LoginSuccessRedirect;
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests( customizers -> customizers
                                .requestMatchers(
                                        "/api/auth/**",         // Allow Auth Endpoints
                                        "/spotify/queue",
                                        "/spotify/callback",
                                        "/completion",
                                        "/songs",
                                        "/queue",
                                        "/spotify/loggedin",
                                        "/login/**",
                                        "/error",
                                        "/oauth2/**",           // OAuth2-Endpunkte
                                        "/login/oauth2/**",     // OAuth2-Login-Endpunkte
                                        "/logout"
                                        ).permitAll()
                                .anyRequest().authenticated()
                        )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(tenantFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/spotify")
                        .successHandler((request, response, authentication) -> {
                            // TODO: Implement logged in user endpoint to return user details if needed
                            redirectStrategy.sendRedirect(request,response,oauth2LoginSuccessRedirect);
                        })
                        .failureHandler(new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    AuthenticationException exception
                            ) throws IOException {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json;charset=UTF-8");

                                Map<String, Object> errorDetails = new HashMap<>();
                                errorDetails.put("timestamp", new Date());
                                errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
                                errorDetails.put("error", "Authentifizierung fehlgeschlagen");
                                errorDetails.put("message", exception.getMessage());

                                // Zusätzliche Details je nach Exception-Typ
                                if (exception instanceof OAuth2AuthenticationException) {
                                    OAuth2Error oauth2Error = ((OAuth2AuthenticationException) exception).getError();
                                    errorDetails.put("oauth2_error_code", oauth2Error.getErrorCode());
                                    errorDetails.put("oauth2_error_description", oauth2Error.getDescription());
                                }

                                // Stack Trace nur in Entwicklungsumgebung
                                if (Arrays.asList("dev", "local").contains(
                                        System.getProperty("spring.profiles.active"))) {
                                    StringWriter sw = new StringWriter();
                                    exception.printStackTrace(new PrintWriter(sw));
                                    errorDetails.put("trace", sw.toString());
                                }

                                ObjectMapper mapper = new ObjectMapper();
                                response.getWriter().write(mapper.writeValueAsString(errorDetails));
                            }
                        })
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(this.oauth2UserService())
                        )
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
