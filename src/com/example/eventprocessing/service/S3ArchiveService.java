package com.example.eventprocessing.service;

import com.example.eventprocessing.model.ProcessingEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class S3ArchiveService {
    private final Path archiveRoot;

    public S3ArchiveService(Path archiveRoot) {
        this.archiveRoot = archiveRoot;
    }

    public String archive(ProcessingEvent event) throws IOException {
        Files.createDirectories(archiveRoot);
        Path archiveFile = archiveRoot.resolve(event.getEventId() + ".txt");
        String content = "eventId=" + event.getEventId() + System.lineSeparator()
                + "eventType=" + event.getEventType() + System.lineSeparator()
                + "createdAt=" + event.getCreatedAt() + System.lineSeparator()
                + "payload=" + event.getPayload() + System.lineSeparator();
        Files.writeString(archiveFile, content, StandardCharsets.UTF_8);
        return archiveFile.toAbsolutePath().toString();
    }
}
