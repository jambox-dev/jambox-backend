package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.SpotifyToken;
import org.jambox.backend.model.SpotifyTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SpotifyAuthService {
    public static final String SCOPES = "user-read-private user-read-email user-modify-playback-state user-read-currently-playing user-read-playback-state";

    @Value( "${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value( "${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value( "${jambox.spotify.callback-url}")
    private String callbackUrl;

    private final Map<String, String> pendingVerifiers = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();


    public String redirectToAuthCodeFlow(String clientId, String tenantId) {
        String verifier = generateCodeVerifier(128);
        pendingVerifiers.put(tenantId, verifier);
        String challenge = generateCodeChallenge(verifier);

        String params = "client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + java.net.URLEncoder.encode(callbackUrl, StandardCharsets.UTF_8) +
                "&scope=" + java.net.URLEncoder.encode(SCOPES, StandardCharsets.UTF_8) +
                "&code_challenge_method=S256" +
                "&code_challenge=" + challenge +
                "&state=" + tenantId;

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

    public SpotifyToken getAccessToken(String code, String tenantId) {
        String verifier = pendingVerifiers.remove(tenantId);
        if (verifier == null) {
            throw new IllegalStateException("No pending verification for tenant: " + tenantId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", callbackUrl);
        params.add("code_verifier", verifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return getSpotifyToken(request, null);
    }

    public SpotifyToken refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return getSpotifyToken(request, refreshToken);
    }

    private SpotifyToken getSpotifyToken(HttpEntity<MultiValueMap<String, String>> request, String existingRefreshToken) {
        ResponseEntity<SpotifyTokenResponse> response = restTemplate.exchange(
                "https://accounts.spotify.com/api/token",
                HttpMethod.POST,
                request,
                SpotifyTokenResponse.class
        );

        if (response.getBody() != null) {
            String newRefreshToken = response.getBody().getRefresh_token();
            if (newRefreshToken == null || newRefreshToken.isBlank()) {
                newRefreshToken = existingRefreshToken;
            }
            return new SpotifyToken(
                    response.getBody().getAccess_token(),
                    newRefreshToken,
                    Math.toIntExact(response.getBody().getExpires_in())
            );
        }
        throw new RuntimeException("Token-Antwort ist null");
    }
}
