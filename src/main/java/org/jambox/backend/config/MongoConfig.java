package org.jambox.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // Weitere MongoDB-spezifische Konfigurationen können hier hinzugefügt werden
}
