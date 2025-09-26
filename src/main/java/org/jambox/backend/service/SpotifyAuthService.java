package org.jambox.backend.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jambox.backend.model.SpotifyToken;
import org.jambox.backend.model.SpotifyTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SpotifyAuthService {
    public static final String SCOPES = "user-read-private user-read-email user-modify-playback-state user-read-currently-playing user-read-playback-state";

    @Value( "${jambox.spotify.client-id}")
    private String clientId;

    @Value( "${jambox.spotify.client-secret}")
    private String clientSecret;

    @Value( "${jambox.spotify.callback-url}")
    private String callbackUrl;

    @Setter
    private SpotifyToken token;

    @Getter
    private String verifier = null ;

    private final RestTemplate restTemplate = new RestTemplate();


    public String redirectToAuthCodeFlow(String clientId) {
        verifier = generateCodeVerifier(128);
        String challenge = generateCodeChallenge(verifier);

        String params = "client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + java.net.URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8) +
                "&scope=" + java.net.URLEncoder.encode(SCOPES, StandardCharsets.UTF_8) +
                "&code_challenge_method=S256" +
                "&code_challenge=" + challenge;

        return "https://accounts.spotify.com/authorize?" + params;
    }

    private String generateCodeVerifier(@SuppressWarnings("SameParameterValue") int length) {
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < length; i++) {
            text.append(possible.charAt(secureRandom.nextInt(possible.length())));
        }
        return text.toString();
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            byte[] bytes = codeVerifier.getBytes(StandardCharsets.UTF_8);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);

            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.replace('+', '-')
                    .replace('/', '_')
                    .replace("=", "");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithmus nicht verfügbar", e);
        }
    }

    public SpotifyToken getAccessToken(String code, String verifier) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", callbackUrl);
        params.add("code_verifier", verifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<SpotifyTokenResponse> response = restTemplate.exchange(
                "https://accounts.spotify.com/api/token",
                HttpMethod.POST,
                request,
                SpotifyTokenResponse.class
        );

        if (response.getBody() != null) {
            return new SpotifyToken(response.getBody().getAccess_token(), response.getBody().getRefresh_token(), Math.toIntExact(response.getBody().getExpires_in()));
        }
        throw new RuntimeException("Token-Antwort ist null");
    }


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

        ResponseEntity<LocalSpotifyToken> response = restTemplate.postForEntity(url, request, LocalSpotifyToken.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            LocalSpotifyToken resp = response.getBody();
            token.setAccessToken(resp.getAccess_token());
            token.setExpiresAt(Instant.now().plusSeconds(resp.getExpires_in()));
        } else {
            throw new RuntimeException("Spotify token refresh failed");
        }
    }

    // DTO für Spotify Token Response
    @Getter
    @Setter
    static class LocalSpotifyToken {
        private String access_token;
        private int expires_in;
    }
}
