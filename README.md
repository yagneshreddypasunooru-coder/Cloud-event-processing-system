# Yagnesh Reddy Cloud-Native Event Processing System

This project was built for Yagnesh Reddy as a locally runnable Java web application that simulates an AWS-style event-driven backend using:

- `AWS Lambda` style request processing
- `DynamoDB` style event state tracking
- `S3` style payload archival
- `CloudWatch` style logging and metrics
- asynchronous event ingestion and processing
- a browser dashboard on `localhost:8080`

It is designed to reflect a realistic resume project while still running on a machine with only `javac` and `java`.

## Project Summary

Yagnesh Reddy developed this system to demonstrate cloud-native backend engineering with asynchronous event processing, Lambda-style execution, DynamoDB-style state tracking, S3-style archival, and CloudWatch-style monitoring.

## Project Structure

- `src/com/example/eventprocessing/app/Main.java`: demo entrypoint
- `src/com/example/eventprocessing/service/DashboardServer.java`: local HTTP server and API endpoints
- `src/com/example/eventprocessing/service/EventIngressApi.java`: serverless API-style publisher
- `src/com/example/eventprocessing/service/AsyncEventPipeline.java`: async processing queue
- `src/com/example/eventprocessing/service/EventProcessorLambda.java`: Lambda-style processor
- `src/com/example/eventprocessing/store/InMemoryDynamoDbEventStore.java`: DynamoDB-style store
- `src/com/example/eventprocessing/service/S3ArchiveService.java`: local S3-style archival
- `src/com/example/eventprocessing/service/CloudWatchLogger.java`: logs and metrics
- `static/`: browser UI assets

## Run

```bash
cd /Users/yagneshreddy/Desktop/yagnesh-reddy-cloud-native-event-processing-system
sh run.sh
```

## Output

- the dashboard is available at `http://localhost:8080`
- processed and failed events are logged to the console
- archived payload files are written into `local-s3-bucket/`
- final event states are printed like a DynamoDB table snapshot
- metrics are printed like a CloudWatch summary

## Next AWS Upgrade Path

To convert this into a real AWS deployment:

1. Replace the in-memory store with the AWS SDK DynamoDB client.
2. Replace local file archival with S3 `PutObject`.
3. Expose `EventIngressApi` through API Gateway + Lambda.
4. Swap the in-process queue for SQS or EventBridge.
5. Add IaC using AWS SAM, CDK, or Terraform.
