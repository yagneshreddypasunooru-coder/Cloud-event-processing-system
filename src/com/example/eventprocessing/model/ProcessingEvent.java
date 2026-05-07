package com.example.eventprocessing.model;

import java.time.Instant;

public class ProcessingEvent {
    private final String eventId;
    private final EventType eventType;
    private final String payload;
    private final Instant createdAt;

    public ProcessingEvent(String eventId, EventType eventType, String payload, Instant createdAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
