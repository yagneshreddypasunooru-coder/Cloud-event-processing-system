package com.example.eventprocessing.service;

import com.example.eventprocessing.model.EventStatus;
import com.example.eventprocessing.model.EventType;
import com.example.eventprocessing.model.ProcessingEvent;
import com.example.eventprocessing.model.ProcessingRecord;
import com.example.eventprocessing.store.EventStore;
import com.example.eventprocessing.util.DemoTimeline;

import java.util.UUID;

public class EventIngressApi {
    private final EventStore eventStore;
    private final AsyncEventPipeline pipeline;

    public EventIngressApi(EventStore eventStore, AsyncEventPipeline pipeline) {
        this.eventStore = eventStore;
        this.pipeline = pipeline;
    }

    public void publish(ProcessingEvent event) {
        eventStore.save(new ProcessingRecord(event.getEventId(), event.getEventType(), EventStatus.PENDING));
        pipeline.submit(event);
    }

    public ProcessingEvent publish(EventType eventType, String payload) {
        ProcessingEvent event = new ProcessingEvent(UUID.randomUUID().toString(), eventType, payload, DemoTimeline.february(26, 11, 20));
        publish(event);
        return event;
    }
}
