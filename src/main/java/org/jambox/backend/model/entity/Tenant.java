package org.jambox.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "tenants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    private String id;

    private String name;

    private String ownerId;

    @Indexed(unique = true)
    private String subdomain;

    @CreatedDate
    private LocalDateTime createdAt;

    private String spotifyRefreshToken;

    private String spotifyAccessToken;

    private LocalDateTime spotifyTokenExpiresAt;
}
