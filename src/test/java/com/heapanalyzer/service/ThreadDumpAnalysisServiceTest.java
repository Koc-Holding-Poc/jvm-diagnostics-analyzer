package com.heapanalyzer.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadDumpAnalysisServiceTest {

    private final ThreadDumpAnalysisService service = new ThreadDumpAnalysisService();

    @Test
    void analyzeMultiple_shouldIncludeComparativeSections() throws Exception {
        Path trustedBase = Path.of("./heap-dumps").toAbsolutePath().normalize();
        Files.createDirectories(trustedBase);
        Path trustedTempDir = Files.createTempDirectory(trustedBase, "thread-dump-test-");
        Path dump1 = trustedTempDir.resolve("dump-1.txt");
        Path dump2 = trustedTempDir.resolve("dump-2.txt");
        Path dump3 = trustedTempDir.resolve("dump-3.txt");

        try {
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
        } finally {
            try (var paths = Files.walk(trustedTempDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
}
