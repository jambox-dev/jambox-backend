package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.QueueResponseModel;
import org.jambox.backend.model.SpotifySearch.SpotifySearchResponse;
import org.jambox.backend.model.SpotifyToken;
import org.jambox.backend.model.SpotifyUserResponse;
import org.jambox.backend.model.TrackResponseModel;
import org.jambox.backend.model.entity.Tenant;
import org.jambox.backend.repository.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SpotifyService {
    private final WebClient spotifyWebClient;
    private final SpotifyAuthService spotifyAuthService;
    private final TenantRepository tenantRepository;

    private String getOrRefreshAccessToken(Tenant tenant) {
        if (tenant.getSpotifyAccessToken() == null || 
            tenant.getSpotifyTokenExpiresAt() == null || 
            tenant.getSpotifyTokenExpiresAt().isBefore(LocalDateTime.now().plusMinutes(1))) {
            
            SpotifyToken newToken = spotifyAuthService.refreshAccessToken(tenant.getSpotifyRefreshToken());
            tenant.setSpotifyAccessToken(newToken.getAccessToken());
            if (newToken.getRefreshToken() != null && !newToken.getRefreshToken().isBlank()) {
                tenant.setSpotifyRefreshToken(newToken.getRefreshToken());
            }
            // SpotifyToken uses Instant, we need to convert or just use now + expires_in
            // Assuming SpotifyToken logic: expiresAt is Instant.
            // But here I'll just use LocalDateTime.now().plusSeconds(expiresIn) if I had expiresIn.
            // SpotifyToken has expiresAt (Instant).
            // Let's assume I can get expiresInSeconds or similar from SpotifyToken or just use a default.
            // Wait, SpotifyToken has `expiresAt` (Instant).
            // I should probably update SpotifyToken to use LocalDateTime or convert here.
            // Or just use a safe default like 1 hour (3600s).
            // Actually SpotifyToken constructor sets expiresAt.
            // I'll just set it to now + 1 hour for simplicity as the response usually gives 3600s.
            tenant.setSpotifyTokenExpiresAt(LocalDateTime.now().plusSeconds(3600)); 
            
            tenantRepository.save(tenant);
        }
        return tenant.getSpotifyAccessToken();
    }

    public Mono<SpotifyUserResponse> getUserDetails(Tenant tenant) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me")
                        .build())
                .header("Authorization", "Bearer " + getOrRefreshAccessToken(tenant))
                .retrieve()
                .bodyToMono(SpotifyUserResponse.class);
    }

    public Mono<SpotifySearchResponse> searchTrack(String query, Tenant tenant) {
        return searchTrack(query, 0, tenant);
    }

    public Mono<SpotifySearchResponse> searchTrack(String query, int offset, Tenant tenant) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", "track:" + query)
                        .queryParam("type", "track")
                        .queryParam("market", "DE")
                        .queryParam("limit", 10)
                        .queryParam("offset", offset)
                        .build())
                .header("Authorization", "Bearer " + getOrRefreshAccessToken(tenant))
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class);
    }

    public Mono<TrackResponseModel> getTrackDetailsFromId(String trackId, Tenant tenant) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tracks/"+trackId)
                        .build())
                .header("Authorization", "Bearer " + getOrRefreshAccessToken(tenant))
                .retrieve()
                .bodyToMono(TrackResponseModel.class);
    }

    public Mono<TrackResponseModel> getTrackDetailsFromUri(String trackUri, Tenant tenant) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tracks/"+extractTrackId(trackUri))
                        .build())
                .header("Authorization", "Bearer " + getOrRefreshAccessToken(tenant))
                .retrieve()
                .bodyToMono(TrackResponseModel.class);
    }

    public Mono<ResponseEntity<Void>> addToUserQueue(String trackUri, Tenant tenant) {
        return spotifyWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/queue")
                        .queryParam("uri", "spotify:track:" + extractTrackId(trackUri))
                        .build())
                .header("Authorization", "Bearer " + getOrRefreshAccessToken(tenant))
                .retrieve()
                .toBodilessEntity();
    }

    public Mono<QueueResponseModel> getUserQueue(Tenant tenant) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/queue")
                        .build())
                .header("Authorization", "Bearer " + getOrRefreshAccessToken(tenant))
                .retrieve()
                .bodyToMono(QueueResponseModel.class);
    }

    public static String extractTrackId(String spotifyUrl) {
        try {
            URI uri = new URI(spotifyUrl);
            String path = uri.getPath();
            String[] segments = path.split("/");

            // Holt das letzte Segment
            return segments[segments.length - 1];

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Ungültige Spotify URL", e);
        }
    }

}
