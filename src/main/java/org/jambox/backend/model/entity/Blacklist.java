package org.jambox.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "blacklist")
@Data
@NoArgsConstructor
public class Blacklist {
    @Id
    @JsonIgnore
    private String id;

    @NotNull
    @JsonProperty("song_url")
    private String songUrl;

    @Indexed
    private String tenantId;
}
