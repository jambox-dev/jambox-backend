package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Tenant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends MongoRepository<Tenant, String> {
    Optional<Tenant> findBySubdomain(String subdomain);
    boolean existsBySubdomain(String subdomain);
}
