package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.Blacklist;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.service.BlacklistService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
public class BlacklistController {
    private final BlacklistService blacklistService;

    @PostMapping
    public void addToBlacklist(@RequestBody Blacklist blacklist){
        blacklistService.addSongToBlacklist(blacklist.getSongUrl());
    }

    @DeleteMapping
    public void removeFromBlacklist(@RequestBody Blacklist blacklist){
        blacklistService.removeFromBlacklist(blacklist.getSongUrl());
    }

    @GetMapping
    public List<Song> getBlacklist(){
        return blacklistService.getBlacklist().isEmpty() ? List.of() : blacklistService.getBlacklist();
    }
}
