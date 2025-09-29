package org.jambox.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "jambox.auth")
@Getter
@Setter
public class AuthProperties {
    private List<String> authorizedEmails;
}

