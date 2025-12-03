package org.jambox.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jambox.backend.model.enums.AuthProvider;
import org.jambox.backend.model.enums.Role;
import org.jambox.backend.model.enums.SubscriptionStatus;
import org.jambox.backend.model.enums.SubscriptionTier;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    private AuthProvider provider;

    private String providerId;

    private Set<Role> roles;

    @Indexed
    private String tenantId;

    private SubscriptionTier subscriptionTier;

    private SubscriptionStatus subscriptionStatus;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
