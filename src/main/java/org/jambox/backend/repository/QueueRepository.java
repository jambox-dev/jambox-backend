package org.jambox.backend.repository;

import org.jambox.backend.model.entity.Queue;
import org.jambox.backend.model.entity.Song;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QueueRepository extends MongoRepository<Queue, String> {
}
