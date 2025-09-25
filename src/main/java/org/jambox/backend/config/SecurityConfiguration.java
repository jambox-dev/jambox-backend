package org.jambox.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.Security;

@Configurable
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
//todo: einkommentieren

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
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
}
