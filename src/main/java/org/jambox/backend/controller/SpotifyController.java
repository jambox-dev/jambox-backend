package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.service.SpotifyAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyAuthService spotifyAuthService;

    @Value("${jambox.spotify.client-id}")
    private String clientId;

    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code) {
        spotifyAuthService.setToken(spotifyAuthService.getAccessToken(code, spotifyAuthService.getVerifier()));
        return "success";
    }

    @GetMapping("/login")
    public ResponseEntity<Object> login() throws URISyntaxException {
        URI spotify = new URI(spotifyAuthService.redirectToAuthCodeFlow(clientId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(spotify);
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }

}
