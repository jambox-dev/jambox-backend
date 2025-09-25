package org.jambox.backend.model.SpotifySearch;

import lombok.Data;

import java.util.List;

@Data
public class SpotifySearchResponseItem {
    private Album album;
    private List<ArtistX> artists;
    private int discNumber;
    private int durationMs;
    private boolean explicit;
    private ExternalIds externalIds;
    private ExternalUrlsXXX externalUrls;
    private String href;
    private String id;
    private boolean isLocal;
    private boolean isPlayable;
    private String name;
    private int popularity;
    private Object previewUrl;
    private int trackNumber;
    private String type;
    private String uri;
}
