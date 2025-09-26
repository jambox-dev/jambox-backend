package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.TrackResponseModel;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.repository.SongRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {
    private final SongRepository songRepository;
    private final SpotifyService spotifyService;

    public Song getDetailsByUrl(String songUrl) {
        Song song = songRepository.findBySongUrl(songUrl).orElse(null);
        if (song == null) {
            TrackResponseModel trackDetails = spotifyService.getTrackDetails(songUrl).block();
            song = new Song();
            if (trackDetails == null || trackDetails.getName() == null) {
                throw new IllegalArgumentException("TrackDetails not found");
            }
            song.setSongName(trackDetails.getName());
            song.setSongUrl(trackDetails.getUri());
            song.setSongCover(trackDetails.getAlbum().getImages().getFirst().getUrl());
            String artists = toCommaSeparatedString(trackDetails.getArtists().stream().map(TrackResponseModel.ArtistInfo::getName).toList());
            song.setAuthor(artists);
            songRepository.save(song);
        }
        return song;
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

    public String toCommaSeparatedString(List<String> artists) {
        StringBuilder result = new StringBuilder();

        artists.forEach(item -> {
            if (!item.isBlank()){
                if (!result.isEmpty()) {
                    result.append(", ");
                }
                result.append(item);
            }
        });

        return result.toString();
    }
}
