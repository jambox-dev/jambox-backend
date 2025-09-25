package org.jambox.backend.model.SpotifySearch;

import lombok.Data;

import java.util.List;

@Data
public class Album {
    private String albumType;
    private List<ArtistX> artists;
    private ExternalUrlsXXX externalUrls;
    private String href;
    private String id;
    private List<Image> images;
    private boolean isPlayable;
    private String name;
    private String releaseDate;
    private String releaseDatePrecision;
    private int totalTracks;
    private String type;
    private String uri;
}
