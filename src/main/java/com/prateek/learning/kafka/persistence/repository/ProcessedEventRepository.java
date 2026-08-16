package com.prateek.learning.kafka.persistence.repository;

import com.prateek.learning.kafka.persistence.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
