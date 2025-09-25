package org.jambox.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "songs")
public class Song {
    @Id
    @JsonIgnore
    private String id;

    @NotNull
    private String songUrl;

    @NotNull
    private String songName;

    @NotNull
    private String author;

    @NotNull
    private String songCover;
}
