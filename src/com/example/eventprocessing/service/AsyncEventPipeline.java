package com.example.eventprocessing.service;

import com.example.eventprocessing.model.ProcessingEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncEventPipeline {
    private final BlockingQueue<ProcessingEvent> queue = new LinkedBlockingQueue<>();
    private final EventProcessorLambda lambda;
    private final CloudWatchLogger logger;
    private Thread workerThread;
    private volatile boolean running;

    public AsyncEventPipeline(EventProcessorLambda lambda, CloudWatchLogger logger) {
        this.lambda = lambda;
        this.logger = logger;
    }

    public void start() {
        running = true;
        workerThread = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    ProcessingEvent event = queue.poll();
                    if (event == null) {
                        Thread.sleep(100);
                        continue;
                    }

                    lambda.handleRequest(event);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.error("Pipeline interrupted");
                    break;
                }
            }
        }, "event-pipeline-worker");
        workerThread.start();
    }

    public void submit(ProcessingEvent event) {
        queue.offer(event);
        logger.incrementMetric("events_received");
        logger.info("Event submitted to async pipeline: " + event.getEventId());
    }

    public int getQueueDepth() {
        return queue.size();
    }

    public void awaitDrain(long pollMillis) throws InterruptedException {
        while (!queue.isEmpty()) {
            Thread.sleep(pollMillis);
        }

        Thread.sleep(pollMillis);
    }

    public void stop() throws InterruptedException {
        running = false;
        if (workerThread != null) {
            workerThread.join();
        }
    }
}
