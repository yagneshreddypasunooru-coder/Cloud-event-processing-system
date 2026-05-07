package com.example.eventprocessing.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CloudWatchLogger {
    private final Map<String, Integer> counters = new ConcurrentHashMap<>();
    private final List<String> recentLogs = new CopyOnWriteArrayList<>();

    public void info(String message) {
        log("INFO", message, false);
    }

    public void error(String message) {
        log("ERROR", message, true);
    }

    public void incrementMetric(String metricName) {
        counters.merge(metricName, 1, Integer::sum);
    }

    public Map<String, Integer> snapshotMetrics() {
        return Map.copyOf(counters);
    }

    public List<String> recentLogs() {
        return new ArrayList<>(recentLogs);
    }

    public void printMetrics() {
        info("CloudWatch metrics snapshot:");
        counters.forEach((name, value) -> info("  " + name + "=" + value));
    }

    private void log(String level, String message, boolean stderr) {
        String line = Instant.now() + " [" + level + "] " + message;
        recentLogs.add(line);
        if (recentLogs.size() > 100) {
            recentLogs.remove(0);
        }

        if (stderr) {
            System.err.println(line);
            return;
        }

        System.out.println(line);
    }
}
