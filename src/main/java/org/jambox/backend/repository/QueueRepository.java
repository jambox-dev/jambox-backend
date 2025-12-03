package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Queue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueueRepository extends MongoRepository<Queue, String> {
    List<Queue> findAllByTenantId(String tenantId);
}
