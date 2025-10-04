package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Blacklist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistRepository extends MongoRepository<Blacklist, String> {
    boolean existsBySongUrl(String songUrl);

    void deleteBySongUrl(String songUrl);
}
