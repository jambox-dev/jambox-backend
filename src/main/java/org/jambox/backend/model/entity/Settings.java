package org.jambox.backend.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "settings")
@Data
public class Settings {
    @Id
    private String id;

    @Indexed(unique = true)
    private String tenantId;

    private boolean needsApproval;
    private boolean isBlacklistEnabled;
}
