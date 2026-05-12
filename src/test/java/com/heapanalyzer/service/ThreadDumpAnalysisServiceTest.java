package com.heapanalyzer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadDumpAnalysisServiceTest {

    private final ThreadDumpAnalysisService service = new ThreadDumpAnalysisService();

    @TempDir
    Path tempDir;

    @Test
    void analyze_shouldSummarizeStatesAndHighlightBlockedAndRunnableThreads() throws IOException {
        Path dumpFile = writeThreadDump("""
                "main" #1 prio=5 os_prio=0 tid=0x1 nid=0x1 runnable
                   java.lang.Thread.State: RUNNABLE
                        at java.lang.Object.wait(Native Method)

                "worker-1" #2 prio=5 os_prio=0 tid=0x2 nid=0x2 waiting for monitor entry
                   java.lang.Thread.State: BLOCKED
                        at com.example.Worker.run(Worker.java:42)

                "scheduler-1" #3 prio=5 os_prio=0 tid=0x3 nid=0x3 waiting on condition
                   java.lang.Thread.State: TIMED_WAITING
                        at java.lang.Thread.sleep(Native Method)
                """);

        String report = service.analyze(dumpFile);

        assertTrue(report.contains("Total threads found: 3"));
        assertTrue(report.contains("RUNNABLE        :    1"));
        assertTrue(report.contains("BLOCKED         :    1"));
        assertTrue(report.contains("TIMED_WAITING   :    1"));
        assertTrue(report.contains("### Deadlock Check: No deadlocks detected"));
        assertTrue(report.contains("### Blocked Threads (1)"));
        assertTrue(report.contains("  - worker-1"));
        assertTrue(report.contains("### Active Runnable Threads (top 20)"));
        assertTrue(report.contains("  - main"));
    }

    @Test
    void analyze_shouldIncludeDeadlockSectionWhenPresent() throws IOException {
        Path dumpFile = writeThreadDump("""
                "t1" #1 prio=5 os_prio=0 tid=0x1 nid=0x1 waiting for monitor entry
                   java.lang.Thread.State: BLOCKED
                        - waiting to lock <0x00000001> (a java.lang.Object)

                Found one Java-level deadlock:
                =============================
                "t1":
                  waiting to lock monitor 0x00000001
                  which is held by "t2"

                "t2" #2 prio=5 os_prio=0 tid=0x2 nid=0x2 waiting for monitor entry
                   java.lang.Thread.State: BLOCKED
                        - waiting to lock <0x00000002> (a java.lang.Object)
                """);

        String report = service.analyze(dumpFile);

        assertTrue(report.contains("### ⚠️ DEADLOCK DETECTED"));
        assertTrue(report.contains("Found one Java-level deadlock:"));
        assertTrue(report.contains("which is held by \"t2\""));
    }

    @Test
    void analyze_shouldLimitRunnableSummaryAndTruncateOversizedRawDump() throws IOException {
        StringBuilder dump = new StringBuilder();
        for (int i = 1; i <= 25; i++) {
            dump.append(String.format("""
                    "thread-%02d" #1 prio=5 os_prio=0 tid=0x%d nid=0x%d runnable
                       java.lang.Thread.State: RUNNABLE
                            at com.example.Task.run(Task.java:%d)

                    """, i, i, i, i));
        }
        dump.append("x".repeat(35_000));

        Path dumpFile = writeThreadDump(dump.toString());

        String report = service.analyze(dumpFile);

        assertTrue(report.contains("Total threads found: 25"));
        assertTrue(report.contains("[... THREAD DUMP TRUNCATED ...]"));

        String runnableSection = report.substring(
                report.indexOf("### Active Runnable Threads (top 20)"),
                report.indexOf("## Raw Thread Dump (for detailed analysis)")
        );

        long runnableLines = runnableSection.lines()
                .filter(line -> line.startsWith("  - "))
                .count();

        assertEquals(20, runnableLines);
        assertTrue(runnableSection.contains("thread-20"));
    }

    private Path writeThreadDump(String content) throws IOException {
        Path dumpFile = tempDir.resolve("thread-dump.txt");
        Files.writeString(dumpFile, content);
        return dumpFile;
    }
}
