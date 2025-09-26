package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Queue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueRepository extends MongoRepository<Queue, String> {
}
