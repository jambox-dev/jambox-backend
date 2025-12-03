package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Blacklist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlacklistRepository extends MongoRepository<Blacklist, String> {
    List<Blacklist> findAllByTenantId(String tenantId);
    boolean existsBySongUrlAndTenantId(String songUrl, String tenantId);
    void deleteBySongUrlAndTenantId(String songUrl, String tenantId);
}
