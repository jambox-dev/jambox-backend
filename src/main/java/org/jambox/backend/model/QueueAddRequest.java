package org.jambox.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class QueueAddRequest {
    @JsonProperty("song_url")
    private String songUrl;
}
