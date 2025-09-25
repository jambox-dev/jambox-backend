package org.jambox.backend.model.SpotifySearch;

import lombok.Data;

import java.util.List;

@Data
public class Tracks {
    private List<SpotifySearchResponseItem> items;
}
