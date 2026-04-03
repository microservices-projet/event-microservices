package com.example.analyticsstreams.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AnalyticsSnapshotRepository extends MongoRepository<AnalyticsSnapshot, String> {
}
