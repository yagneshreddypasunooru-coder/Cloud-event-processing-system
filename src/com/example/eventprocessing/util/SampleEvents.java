package com.example.eventprocessing.util;

import com.example.eventprocessing.model.EventType;
import com.example.eventprocessing.model.ProcessingEvent;

import java.util.List;
import java.util.UUID;

public final class SampleEvents {
    private SampleEvents() {
    }

    public static List<ProcessingEvent> build() {
        return List.of(
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.ORDER_CREATED,
                        "orderId=1001,customerId=501,total=149.99", DemoTimeline.january(15, 10, 30)),
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.PAYMENT_RECEIVED,
                        "paymentId=2001,orderId=1001,status=confirmed", DemoTimeline.january(28, 14, 45)),
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.FILE_UPLOADED,
                        "fileKey=invoices/2026/02/report.csv,size=4MB", DemoTimeline.february(10, 9, 15)),
                new ProcessingEvent(UUID.randomUUID().toString(), EventType.FILE_UPLOADED,
                        "fail:file-corruption-detected", DemoTimeline.february(22, 16, 5))
        );
    }
}
