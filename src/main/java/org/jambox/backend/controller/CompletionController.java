package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.SpotifySearch.SpotifySearchResponseItem;
import org.jambox.backend.service.SpotifyService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/completion")
public class CompletionController {

    final SpotifyService spotifyService;

    @GetMapping
    public Mono<String[]> getCompletion(@RequestParam String search) {
        return spotifyService.searchTrack(search)
                .map(response -> response.getTracks().getItems().stream()
                        .map(SpotifySearchResponseItem::getName)
                        .limit(5)
                        .toArray(String[]::new)
                );
    }
}
