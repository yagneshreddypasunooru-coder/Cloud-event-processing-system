package com.example.eventprocessing.store;

import com.example.eventprocessing.model.ProcessingRecord;

import java.util.Collection;
import java.util.Optional;

public interface EventStore {
    void save(ProcessingRecord record);

    Optional<ProcessingRecord> findById(String eventId);

    Collection<ProcessingRecord> findAll();
}
