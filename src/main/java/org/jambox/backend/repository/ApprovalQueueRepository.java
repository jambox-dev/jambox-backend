package org.jambox.backend.repository;

import org.jambox.backend.model.entity.ApprovalQueue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalQueueRepository extends MongoRepository<ApprovalQueue, String> {
}
