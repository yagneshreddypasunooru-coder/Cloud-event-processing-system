package com.example.eventprocessing.util;

import com.example.eventprocessing.model.EventType;
import com.example.eventprocessing.model.ProcessingEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class SampleEvents {
    private SampleEvents() {
    }

    public static List<ProcessingEvent> build() {
        return List.of(
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.ORDER_CREATED,
                        "orderId=1001,customerId=501,total=149.99", Instant.now()),
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.PAYMENT_RECEIVED,
                        "paymentId=2001,orderId=1001,status=confirmed", Instant.now()),
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.FILE_UPLOADED,
                        "fileKey=invoices/2026/05/report.csv,size=4MB", Instant.now()),
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.FILE_UPLOADED,
                        "fail:file-corruption-detected", Instant.now())
        );
    }
}
