package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Song;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SongRepository extends MongoRepository<Song, String> {
    Optional<Song> findBySongUrl(String songUrl);
    boolean existsBySongUrl(String songUrl);
}
