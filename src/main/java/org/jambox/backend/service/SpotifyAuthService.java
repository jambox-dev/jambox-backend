package org.jambox.backend.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jambox.backend.model.SpotifyToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SpotifyAuthService {
    @Value( "${jambox.spotify.client-id}")
    private String clientId;

    @Value( "${jambox.spotify.client-secret}")
    private String clientSecret;

    @Setter
    private SpotifyToken token;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken() {
        if (token == null) {
            throw new IllegalStateException("No Spotify Token available");
        }
        if (token.isExpired()) {
            refreshToken();
        }
        return token.getAccessToken();
    }

    private void refreshToken() {
        String url = "https://accounts.spotify.com/api/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", token.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<SpotifyTokenResponse> response = restTemplate.postForEntity(url, request, SpotifyTokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            SpotifyTokenResponse resp = response.getBody();
            token.setAccessToken(resp.getAccess_token());
            token.setExpiresAt(Instant.now().plusSeconds(resp.getExpires_in()));
        } else {
            throw new RuntimeException("Spotify token refresh failed");
        }
    }

    // DTO für Spotify Token Response
    @Getter
    @Setter
    static class SpotifyTokenResponse {
        private String access_token;
        private int expires_in;
    }
}
