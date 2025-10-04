package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.Blacklist;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.BlacklistRepository;
import org.jambox.backend.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;
    private final SongRepository songRepository;
    private final SongService songService;

    public void addSongToBlacklist(String songUrl) {
        if (blacklistRepository.existsBySongUrl(songUrl)) {
            return;
        }
        Blacklist blacklistEntity = new Blacklist();
        blacklistEntity.setSongUrl(songUrl);
        blacklistRepository.save(blacklistEntity);
    }

    public boolean isSongInBlacklist(String songUrl) {
        return blacklistRepository.existsBySongUrl(songUrl);
    }

    public List<Song> getBlacklist() {
        // 1. Retrieve all Blacklist entries
        List<Blacklist> blacklistEntries = blacklistRepository.findAll();

        // 2. Extract song URLs from the Blacklist entries
        List<String> songUrls = blacklistEntries.stream()
                .map(Blacklist::getSongUrl)
                .toList();

        // 3. Fetch the corresponding Song objects from the database in a single query
        if (songUrls.isEmpty()) {
            return Collections.emptyList();
        }

        List<Song> resultSongs = new ArrayList<>();

        songUrls.forEach(songUrl -> {
            if (songUrl == null || songUrl.isEmpty()) {
                return;
            }
            if (songRepository.existsBySongUrl(songUrl)) {
                resultSongs.add(songService.getDetailsByUrl(songUrl));
            } else {
                resultSongs.addAll(songRepository.findBySongUrl(songUrl).orElse(new ArrayList<>()));
            }
        });

        return resultSongs;
    }

    public void removeFromBlacklist(String songUrl) {
        blacklistRepository.deleteBySongUrl(songUrl);
    }
}
