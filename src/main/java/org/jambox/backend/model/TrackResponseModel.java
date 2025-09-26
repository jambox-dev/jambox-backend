package org.jambox.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackResponseModel {
    private AlbumInfo album;
    private List<ArtistInfo> artists;
    private List<String> availableMarkets;
    private Integer discNumber;
    private Long durationMs;
    private boolean explicit;
    private Map<String, String> externalIds;
    private Map<String, String> externalUrls;
    private String href;
    private String id;
    private boolean isPlayable;
    private Map<String, String> restrictions;
    private String name;
    private Integer popularity;
    private String previewUrl;
    private Integer trackNumber;
    private String type;
    private String uri;
    private boolean isLocal;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlbumInfo {
        private String albumType;
        private Integer totalTracks;
        private List<String> availableMarkets;
        private Map<String, String> externalUrls;
        private String href;
        private String id;
        private List<ImageInfo> images;
        private String name;
        private String releaseDate;
        private String releaseDatePrecision;
        private Map<String, String> restrictions;
        private String type;
        private String uri;
        private List<ArtistInfo> artists;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArtistInfo {
        private Map<String, String> externalUrls;
        private String href;
        private String id;
        private String name;
        private String type;
        private String uri;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageInfo {
        private String url;
        private Integer height;
        private Integer width;
    }
}
