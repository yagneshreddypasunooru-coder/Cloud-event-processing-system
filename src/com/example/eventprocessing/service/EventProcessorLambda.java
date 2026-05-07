package com.example.eventprocessing.service;

import com.example.eventprocessing.model.EventStatus;
import com.example.eventprocessing.model.ProcessingEvent;
import com.example.eventprocessing.model.ProcessingRecord;
import com.example.eventprocessing.store.EventStore;

public class EventProcessorLambda {
    private final EventStore eventStore;
    private final S3ArchiveService archiveService;
    private final CloudWatchLogger logger;
    private final int maxRetries;

    public EventProcessorLambda(EventStore eventStore, S3ArchiveService archiveService, CloudWatchLogger logger, int maxRetries) {
        this.eventStore = eventStore;
        this.archiveService = archiveService;
        this.logger = logger;
        this.maxRetries = maxRetries;
    }

    public void handleRequest(ProcessingEvent event) {
        ProcessingRecord record = eventStore.findById(event.getEventId())
                .orElse(new ProcessingRecord(event.getEventId(), event.getEventType(), EventStatus.PENDING));

        record.setStatus(EventStatus.PROCESSING);
        eventStore.save(record);
        logger.info("Lambda started for event " + event.getEventId());

        try {
            validate(event);
            String archiveLocation = archiveService.archive(event);
            record.setArchiveLocation(archiveLocation);
            record.setErrorMessage(null);
            record.setStatus(EventStatus.PROCESSED);
            eventStore.save(record);
            logger.incrementMetric("events_processed");
            logger.info("Lambda completed for event " + event.getEventId());
        } catch (Exception ex) {
            record.incrementRetryCount();
            record.setErrorMessage(ex.getMessage());
            record.setStatus(EventStatus.FAILED);
            eventStore.save(record);
            logger.incrementMetric("events_failed");
            logger.error("Lambda failed for event " + event.getEventId() + ": " + ex.getMessage());

            if (record.getRetryCount() <= maxRetries) {
                logger.info("Retry budget remaining for event " + event.getEventId() + ": "
                        + (maxRetries - record.getRetryCount() + 1));
            }
        }
    }

    private void validate(ProcessingEvent event) {
        if (event.getPayload() == null || event.getPayload().isBlank()) {
            throw new IllegalArgumentException("Payload cannot be blank");
        }

        if (event.getPayload().toLowerCase().contains("fail")) {
            throw new IllegalStateException("Injected processing failure for payload: " + event.getPayload());
        }
    }
}
