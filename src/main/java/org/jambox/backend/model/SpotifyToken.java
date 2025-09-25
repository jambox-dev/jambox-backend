package org.jambox.backend.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class SpotifyToken {
    @Setter
    @Getter
    private String accessToken;
    @Getter
    private String refreshToken;
    @Setter
    private Instant expiresAt;

    public SpotifyToken(String accessToken, String refreshToken, int expiresInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = Instant.now().plusSeconds(expiresInSeconds);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt.minusSeconds(30)); // 30s Puffer
    }
}
