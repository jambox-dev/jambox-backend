package org.jambox.backend.controller;

import lombok.RequiredArgsConstructor;
import org.jambox.backend.model.entity.Song;
import org.jambox.backend.model.SpotifySearch.ArtistX;
import org.jambox.backend.model.SpotifySearch.SpotifySearchResponse;
import org.jambox.backend.model.SpotifySearch.SpotifySearchResponseItem;
import org.jambox.backend.model.SpotifySearch.Tracks;
import org.jambox.backend.service.SongService;
import org.jambox.backend.service.SpotifyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {
    private final SpotifyService spotifyService;
    private final SongService songService;

    @GetMapping()
    public Song[] getSongs(@RequestParam(name = "song_name") String songName, @RequestParam(name = "offset") Optional<Integer> offset) {
        if (songName.contains("https://open.spotify.com/track")){
            try {
                String trackId = songName.substring(songName.indexOf("track/") + 6, songName.indexOf("?si"));
                Song song = songService.getDetailsById(trackId);
                return new Song[]{ song };
            } catch (Exception e) {
                return new Song[0];
            }

        }

        ArrayList<Song> songs = new ArrayList<>();
        Tracks tracks = null;
        if (offset.isPresent()) {
            tracks = spotifyService.searchTrack(songName, offset.get()).map(SpotifySearchResponse::getTracks).block();
        } else {
            tracks = spotifyService.searchTrack(songName).map(SpotifySearchResponse::getTracks).block();
        }

        assert tracks != null;
        List<SpotifySearchResponseItem> items = tracks.getItems();

        for (int i = 0; i < items.toArray().length; i++) {
            Song song = new Song();
            song.setSongName(items.get(i).getName());
            song.setSongUrl(items.get(i).getHref());
            song.setSongCover(items.get(i).getAlbum().getImages().getFirst().getUrl());
            String artists = songService.toCommaSeparatedString(items.get(i).getArtists().stream().map(ArtistX::getName).toList());
            song.setAuthor(artists);
            songs.add(song);
        }

        songService.addSongs(songs);
        return songs.toArray(Song[]::new);
    }



}
