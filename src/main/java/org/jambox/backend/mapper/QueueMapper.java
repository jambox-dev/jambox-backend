package org.jambox.backend.mapper;

import org.jambox.backend.model.QueueResponseModel;
import org.jambox.backend.model.TrackResponseModel;
import org.jambox.backend.model.entity.Song;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueueMapper {

    public List<Song> toSongList(QueueResponseModel queueResponse) {
        List<Song> songs = new ArrayList<>();

        // Aktuell spielender Song an Index 0
        if (queueResponse.getCurrentlyPlaying() != null) {
            songs.add(mapTrackToSong(queueResponse.getCurrentlyPlaying()));
        }

        // Queue-Songs hinzufügen
        if (queueResponse.getQueue() != null) {
            songs.addAll(queueResponse.getQueue().stream()
                    .map(this::mapTrackToSong)
                    .toList());
        }

        return songs;
    }

    private Song mapTrackToSong(TrackResponseModel track) {
        if (track == null) {
            return null;
        }

        return Song.builder()
                .songUrl(track.getUri())
                .songName(track.getName())
                .author(getArtistNames(track.getArtists()))
                .songCover(getAlbumImageUrl(track.getAlbum()))
                .build();
    }

    private String getArtistNames(List<TrackResponseModel.ArtistInfo> artists) {
        if (artists == null || artists.isEmpty()) {
            return "";
        }
        return artists.stream()
                .map(TrackResponseModel.ArtistInfo::getName)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.joining(", "));
    }

    private String getAlbumImageUrl(TrackResponseModel.AlbumInfo album) {
        if (album == null || album.getImages() == null || album.getImages().isEmpty()) {
            return null;
        }
        return album.getImages().getFirst().getUrl();
    }
}
