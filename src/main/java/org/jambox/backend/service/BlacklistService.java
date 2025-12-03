package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.config.TenantContext;
import org.jambox.backend.model.entity.Blacklist;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.BlacklistRepository;
import org.jambox.backend.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;
    private final SongRepository songRepository;
    private final SongService songService;

    public void addSongToBlacklist(String songUrl) {
        String tenantId = TenantContext.getTenantId();
        if (blacklistRepository.existsBySongUrlAndTenantId(songUrl, tenantId)) {
            return;
        }
        Blacklist blacklistEntity = new Blacklist();
        blacklistEntity.setSongUrl(songUrl);
        blacklistEntity.setTenantId(tenantId);
        blacklistRepository.save(blacklistEntity);
    }

    public boolean isSongInBlacklist(String songUrl) {
        return blacklistRepository.existsBySongUrlAndTenantId(songUrl, TenantContext.getTenantId());
    }

    public List<Song> getBlacklist() {
        String tenantId = TenantContext.getTenantId();
        // 1. Retrieve all Blacklist entries for tenant
        List<Blacklist> blacklistEntries = blacklistRepository.findAllByTenantId(tenantId);

        // 2. Extract song URLs from the Blacklist entries
        List<String> songUrls = blacklistEntries.stream()
                .map(Blacklist::getSongUrl)
                .toList();

        // 3. Fetch the corresponding Song objects from the database
        if (songUrls.isEmpty()) {
            return Collections.emptyList();
        }

        List<Song> resultSongs = new ArrayList<>();

        songUrls.forEach(songUrl -> {
            if (songUrl == null || songUrl.isEmpty()) {
                return;
            }
            if (songRepository.existsBySongUrlAndTenantId(songUrl, tenantId)) {
                resultSongs.add(songService.getDetailsByUrl(songUrl));
            } else {
                resultSongs.addAll(songRepository.findBySongUrlAndTenantId(songUrl, tenantId).orElse(new ArrayList<>()));
            }
        });

        return resultSongs;
    }

    public void removeFromBlacklist(String songUrl) {
        blacklistRepository.deleteBySongUrlAndTenantId(songUrl, TenantContext.getTenantId());
    }
}
