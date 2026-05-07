package com.example.eventprocessing.app;

import com.example.eventprocessing.service.DashboardServer;
import com.example.eventprocessing.service.AsyncEventPipeline;
import com.example.eventprocessing.service.CloudWatchLogger;
import com.example.eventprocessing.service.EventIngressApi;
import com.example.eventprocessing.service.EventProcessorLambda;
import com.example.eventprocessing.service.S3ArchiveService;
import com.example.eventprocessing.store.EventStore;
import com.example.eventprocessing.store.InMemoryDynamoDbEventStore;
import com.example.eventprocessing.util.SampleEvents;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        String developerName = "Yagnesh Reddy";
        CloudWatchLogger logger = new CloudWatchLogger();
        EventStore eventStore = new InMemoryDynamoDbEventStore();
        S3ArchiveService archiveService = new S3ArchiveService(Path.of("local-s3-bucket"));
        EventProcessorLambda lambda = new EventProcessorLambda(eventStore, archiveService, logger, 2);
        AsyncEventPipeline pipeline = new AsyncEventPipeline(lambda, logger);
        EventIngressApi api = new EventIngressApi(eventStore, pipeline);
        DashboardServer dashboardServer = new DashboardServer(8080, developerName, api, eventStore, pipeline, logger);

        logger.info("Starting Yagnesh Reddy's cloud-native event processing web app");
        logger.info("Developer: " + developerName);
        pipeline.start();
        SampleEvents.build().forEach(api::publish);
        dashboardServer.start();
        logger.info("Dashboard available at http://localhost:8080");
    }
}
