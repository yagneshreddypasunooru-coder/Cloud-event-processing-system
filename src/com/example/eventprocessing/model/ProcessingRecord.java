package com.example.eventprocessing.model;

import com.example.eventprocessing.util.DemoTimeline;

import java.time.Instant;

public class ProcessingRecord {
    private final String eventId;
    private final EventType eventType;
    private EventStatus status;
    private String archiveLocation;
    private String errorMessage;
    private int retryCount;
    private Instant updatedAt;

    public ProcessingRecord(String eventId, EventType eventType, EventStatus status) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.status = status;
        this.updatedAt = DemoTimeline.next();
    }

    public String getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
        this.updatedAt = DemoTimeline.next();
    }

    public String getArchiveLocation() {
        return archiveLocation;
    }

    public void setArchiveLocation(String archiveLocation) {
        this.archiveLocation = archiveLocation;
        this.updatedAt = DemoTimeline.next();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.updatedAt = DemoTimeline.next();
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount += 1;
        this.updatedAt = DemoTimeline.next();
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "ProcessingRecord{" +
                "eventId='" + eventId + '\'' +
                ", eventType=" + eventType +
                ", status=" + status +
                ", archiveLocation='" + archiveLocation + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", retryCount=" + retryCount +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
