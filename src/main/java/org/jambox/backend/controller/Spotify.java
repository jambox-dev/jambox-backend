package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.SpotifyToken;
import org.jambox.backend.service.SpotifyAuthService;
import org.jambox.backend.service.SpotifyService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class Spotify {
//    private final SpotifyAuthService spotifyService;
//
//    @GetMapping("/")
//    public String home() {
//        return "login";
//    }
//    @GetMapping("/profile")
//    public String profile(@RegisteredOAuth2AuthorizedClient("spotify") OAuth2AuthorizedClient authorizedClient) {
//        String token = authorizedClient.getAccessToken().getTokenValue();
//        assert authorizedClient.getRefreshToken() != null;
//        spotifyService.setToken(new SpotifyToken(token, authorizedClient.getRefreshToken().getTokenValue(), Math.toIntExact(Duration.between(Instant.now(), authorizedClient.getAccessToken().getExpiresAt()).getSeconds())));
//        return "success";
//    }
//
//    @GetMapping("/me/playlists")
//    public String getMyPlaylists() {
//        String token = spotifyService.getAccessToken();
//
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(token);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<String> response = restTemplate.exchange(
//                "https://api.spotify.com/v1/me/playlists",
//                HttpMethod.GET,
//                entity,
//                String.class
//        );
//
//        return response.getBody();
//    }
}
