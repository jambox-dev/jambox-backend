package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {
    private final SongRepository songRepository;

    public Song getDetailsByUrl(String songUrl) {
        //todo wenn null dann api ansprechen
        return songRepository.findBySongUrl(songUrl).orElse(null);
    }

    public void addSong(Song song) {
        if (!songRepository.existsBySongUrl(song.getSongUrl())) {
            songRepository.save(song);
        }
    }

    public void addSongs(List<Song> songs) {
        for (Song song : songs) {
            addSong(song);
        }
    }

    public void removeSong(Song song) {
        songRepository.delete(song);
    }
}
