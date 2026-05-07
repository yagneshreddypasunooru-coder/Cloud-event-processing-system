package com.example.eventprocessing.store;

import com.example.eventprocessing.model.ProcessingRecord;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDynamoDbEventStore implements EventStore {
    private final Map<String, ProcessingRecord> records = new ConcurrentHashMap<>();

    @Override
    public void save(ProcessingRecord record) {
        records.put(record.getEventId(), record);
    }

    @Override
    public Optional<ProcessingRecord> findById(String eventId) {
        return Optional.ofNullable(records.get(eventId));
    }

    @Override
    public Collection<ProcessingRecord> findAll() {
        return records.values();
    }
}
