package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.mapper.QueueMapper;
import org.jambox.backend.model.QueueResponseModel;
import org.jambox.backend.model.SpotifyUserResponse;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.service.SpotifyAuthService;
import org.jambox.backend.service.SpotifyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyAuthService spotifyAuthService;
    private final SpotifyService spotifyService;
    private final QueueMapper queueMapper;

    @Value("${jambox.spotify.client-id}")
    private String clientId;

    @Value("${jambox.spotify.email-address}")
    private String spotifyEmailAddress;

    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code) {
        spotifyAuthService.setToken(spotifyAuthService.getAccessToken(code, spotifyAuthService.getVerifier()));
        SpotifyUserResponse user = spotifyService.getUserDetails().block();
        if (user == null) {
            throw new IllegalStateException("User details could not be retrieved");
        }
        if (!Objects.equals(user.getEmail(), spotifyEmailAddress)) {
            return "error: not Allowed to Perform this action";
        }
        return "success";
    }

    @GetMapping("/login")
    public ResponseEntity<Object> login() throws URISyntaxException {
        URI spotify = new URI(spotifyAuthService.redirectToAuthCodeFlow(clientId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(spotify);
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }

    @GetMapping("/loggedin")
    public ResponseEntity<Object> loggedIn() {
        if (spotifyAuthService.getAccessToken() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/queue")
    public Song[] getQueue(){
        QueueResponseModel queue = spotifyService.getUserQueue().block();
        if (queue == null) {
            return new Song[0];
        }
        return queueMapper.toSongList(queue).toArray(Song[]::new);
    }
}