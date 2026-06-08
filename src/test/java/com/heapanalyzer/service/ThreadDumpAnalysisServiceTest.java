package com.heapanalyzer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadDumpAnalysisServiceTest {

    private final ThreadDumpAnalysisService service = new ThreadDumpAnalysisService();

    @TempDir
    Path tempDir;

    @Test
    void analyzeMultiple_shouldIncludeComparativeSections() throws Exception {
        Path dump1 = tempDir.resolve("dump-1.txt");
        Path dump2 = tempDir.resolve("dump-2.txt");
        Path dump3 = tempDir.resolve("dump-3.txt");

        Files.writeString(dump1, """
                "worker-A" #12 prio=5 tid=0x01
                   java.lang.Thread.State: RUNNABLE
                "worker-B" #13 prio=5 tid=0x02
                   java.lang.Thread.State: BLOCKED
                "worker-C" #14 prio=5 tid=0x03
                   java.lang.Thread.State: WAITING
                """);
        Files.writeString(dump2, """
                "worker-A" #12 prio=5 tid=0x01
                   java.lang.Thread.State: BLOCKED
                "worker-B" #13 prio=5 tid=0x02
                   java.lang.Thread.State: BLOCKED
                "worker-D" #15 prio=5 tid=0x04
                   java.lang.Thread.State: RUNNABLE
                """);
        Files.writeString(dump3, """
                "worker-A" #12 prio=5 tid=0x01
                   java.lang.Thread.State: WAITING
                "worker-B" #13 prio=5 tid=0x02
                   java.lang.Thread.State: BLOCKED
                "worker-D" #15 prio=5 tid=0x04
                   java.lang.Thread.State: RUNNABLE
                """);

        String report = service.analyze(List.of(dump1, dump2, dump3));

        assertTrue(report.contains("## Comparative Analysis Across Dumps"));
        assertTrue(report.contains("worker-A : RUNNABLE -> BLOCKED -> WAITING"));
        assertTrue(report.contains("Continuously BLOCKED: worker-B"));
        assertTrue(report.contains("worker-D"));
        assertTrue(report.contains("worker-C"));
        assertTrue(report.contains("Problematic Thread Time Series"));
    }
}
