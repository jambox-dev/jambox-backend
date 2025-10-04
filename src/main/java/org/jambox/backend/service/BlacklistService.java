package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.Blacklist;
import org.jambox.backend.repository.BlacklistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;

    public void addSongToBlacklist(String songUrl){
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

    public List<Blacklist> getBlacklist(){
        return blacklistRepository.findAll();
    }

    public void removeFromBlacklist(String songUrl){
        blacklistRepository.deleteBySongUrl(songUrl);
    }
}
