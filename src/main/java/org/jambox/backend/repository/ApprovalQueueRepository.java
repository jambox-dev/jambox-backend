package org.jambox.backend.repository;

import org.jambox.backend.model.entity.ApprovalQueue;
import org.jambox.backend.model.entity.Queue;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApprovalQueueRepository extends MongoRepository<ApprovalQueue, String> {
}
