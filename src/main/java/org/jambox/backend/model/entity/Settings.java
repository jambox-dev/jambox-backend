package org.jambox.backend.model.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "settings")
@Data
public class Settings {
    @Indexed(unique = true)
    private String id = "SINGLETON";

    private boolean needsApproval;
}
