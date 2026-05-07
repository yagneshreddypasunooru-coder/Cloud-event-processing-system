package com.example.eventprocessing.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class DemoTimeline {
    private static final Instant BASE_TIME = Instant.parse("2026-01-28T09:15:00Z");
    private static final AtomicLong STEP_COUNTER = new AtomicLong(0);

    private DemoTimeline() {
    }

    public static Instant next() {
        return BASE_TIME.plus(STEP_COUNTER.getAndIncrement() * 19, ChronoUnit.HOURS);
    }

    public static Instant january(int day, int hour, int minute) {
        return Instant.parse(String.format("2026-01-%02dT%02d:%02d:00Z", day, hour, minute));
    }

    public static Instant february(int day, int hour, int minute) {
        return Instant.parse(String.format("2026-02-%02dT%02d:%02d:00Z", day, hour, minute));
    }
}
