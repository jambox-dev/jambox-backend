package org.jambox.backend.service;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.config.TenantContext;
import org.jambox.backend.model.TrackResponseModel;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.model.entity.Tenant;
import org.jambox.backend.repository.SongRepository;
import org.jambox.backend.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongService {
    private final SongRepository songRepository;
    private final SpotifyService spotifyService;
    private final TenantRepository tenantRepository;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found"));
    }

    public Song getDetailsByUrl(String songUrl) {
        String tenantId = TenantContext.getTenantId();
        List<Song> songs = songRepository.findBySongUrlAndTenantId(songUrl, tenantId).orElse(null);
        Song song = null;

        if (songs == null || songs.isEmpty()) {
            TrackResponseModel trackDetails = spotifyService.getTrackDetailsFromUri(songUrl, getCurrentTenant()).block();
            song = new Song();
            if (trackDetails == null || trackDetails.getName() == null) {
                throw new IllegalArgumentException("TrackDetails not found");
            }
            song.setSongName(trackDetails.getName());
            song.setSongUrl(trackDetails.getHref());
            song.setSongCover(trackDetails.getAlbum().getImages().getFirst().getUrl());
            String artists = toCommaSeparatedString(trackDetails.getArtists().stream().map(TrackResponseModel.ArtistInfo::getName).toList());
            song.setAuthor(artists);
            song.setTenantId(tenantId);
            songRepository.save(song);
        } else {
            song = songs.getFirst();
        }
        return song;
    }

    //todo: caching
    public Song getDetailsById(String songId) {
        TrackResponseModel trackDetails = spotifyService.getTrackDetailsFromId(songId, getCurrentTenant()).block();
        Song song = new Song();
        if (trackDetails == null || trackDetails.getName() == null) {
            throw new IllegalArgumentException("TrackDetails not found");
        }
        song.setSongName(trackDetails.getName());
        song.setSongUrl(trackDetails.getHref());
        song.setSongCover(trackDetails.getAlbum().getImages().getFirst().getUrl());
        String artists = toCommaSeparatedString(trackDetails.getArtists().stream().map(TrackResponseModel.ArtistInfo::getName).toList());
        song.setAuthor(artists);
        song.setTenantId(TenantContext.getTenantId());
        songRepository.save(song);
        return song;
    }
    public void addSong(Song song) {
        String tenantId = TenantContext.getTenantId();
        if (!songRepository.existsBySongUrlAndTenantId(song.getSongUrl(), tenantId)) {
            song.setTenantId(tenantId);
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
