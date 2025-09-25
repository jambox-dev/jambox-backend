package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.SpotifySearch.SpotifySearchResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
public class SpotifyService {
    private final WebClient spotifyWebClient;
    private final SpotifyAuthService spotifyAuthService;

    public Mono<SpotifySearchResponse> searchTrack(String query) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", "track:" + query)
                        .queryParam("type", "track")
                        .queryParam("market", "DE")
                        .queryParam("limit", 10)
                        .build())
                .header("Authorization", "Bearer " + spotifyAuthService.getAccessToken())
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class);
    }

    public Mono<SpotifySearchResponse> addToUserQueue(String trackUri) {
        return spotifyWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/queue")
                        .queryParam("uri", "spotify%3Atrack%3A" + extractTrackId(trackUri))
                        .build())
                .header("Authorization", "Bearer " + spotifyAuthService.getAccessToken())
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class);
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
