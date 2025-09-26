package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.QueueResponseModel;
import org.jambox.backend.model.SpotifySearch.SpotifySearchResponse;
import org.jambox.backend.model.SpotifyUserResponse;
import org.jambox.backend.model.TrackResponseModel;
import org.springframework.http.ResponseEntity;
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

    public Mono<SpotifyUserResponse> getUserDetails() {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me")
                        .build())
                .header("Authorization", "Bearer " + spotifyAuthService.getAccessToken())
                .retrieve()
                .bodyToMono(SpotifyUserResponse.class);
    }


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

    public Mono<TrackResponseModel> getTrackDetails(String trackUri) {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tracks/"+extractTrackId(trackUri))
                        .build())
                .header("Authorization", "Bearer " + spotifyAuthService.getAccessToken())
                .retrieve()
                .bodyToMono(TrackResponseModel.class);
    }

    public Mono<ResponseEntity<Void>> addToUserQueue(String trackUri) {
        return spotifyWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/queue")
                        .queryParam("uri", "spotify:track:" + extractTrackId(trackUri))
                        .build())
                .header("Authorization", "Bearer " + spotifyAuthService.getAccessToken())
                .retrieve()
                .toBodilessEntity();
    }

    public Mono<QueueResponseModel> getUserQueue() {
        return spotifyWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/player/queue")
                        .build())
                .header("Authorization", "Bearer " + spotifyAuthService.getAccessToken())
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
