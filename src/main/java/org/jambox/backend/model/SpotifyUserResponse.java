package org.jambox.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SpotifyUserResponse {
    private String country;
    
    @JsonProperty("display_name")
    private String displayName;
    
    private String email;
    
    @JsonProperty("explicit_content")
    private ExplicitContent explicitContent;
    
    @JsonProperty("external_urls")
    private Map<String, String> externalUrls;
    
    private Followers followers;
    
    private String href;
    
    private String id;
    
    private List<Image> images;
    
    private String product;
    
    private String type;
    
    private String uri;
}

@Data
class ExplicitContent {
    @JsonProperty("filter_enabled")
    private boolean filterEnabled;
    
    @JsonProperty("filter_locked")
    private boolean filterLocked;
}

@Data
class Followers {
    private String href;
    private int total;
}

@Data
class Image {
    private String url;
    private Integer height;
    private Integer width;
}