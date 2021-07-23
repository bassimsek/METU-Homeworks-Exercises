package com.ceng495.hw2.repository;

import com.ceng495.hw2.model.PlayTimeLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayTimeLogRepository extends MongoRepository<PlayTimeLog, String> {
}
