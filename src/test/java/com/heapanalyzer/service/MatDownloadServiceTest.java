package com.heapanalyzer.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatDownloadServiceTest {

    @TempDir
    Path tempDir;

    private String originalOsName;
    private String originalUserHome;

    @Test
    void getEffectiveMatHome_resolvesNestedMacAppParseScript() throws Exception {
        originalOsName = System.getProperty("os.name");
        System.setProperty("os.name", "Mac OS X");
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());

        Path parseScript = tempDir.resolve(".jvm-diagnostics/mat/MemoryAnalyzer.app/Contents/MacOS/ParseHeapDump.sh");
        Files.createDirectories(parseScript.getParent());
        Files.writeString(parseScript, "#!/bin/sh");

        MatDownloadService service = new MatDownloadService("/opt/mat");

        assertEquals(parseScript.getParent().toString(), service.getEffectiveMatHome());
    }

    @Test
    void getEffectiveMatHome_resolvesOneLevelNestedParseScript() throws Exception {
        originalOsName = System.getProperty("os.name");
        System.setProperty("os.name", "Linux");
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());

        Path parseScript = tempDir.resolve(".jvm-diagnostics/mat/mat/ParseHeapDump.sh");
        Files.createDirectories(parseScript.getParent());
        Files.writeString(parseScript, "#!/bin/sh");

        MatDownloadService service = new MatDownloadService("/opt/mat");

        assertEquals(parseScript.getParent().toString(), service.getEffectiveMatHome());
    }

    @AfterEach
    void restoreSystemProperties() {
        if (originalOsName == null) {
            System.clearProperty("os.name");
        } else {
            System.setProperty("os.name", originalOsName);
        }

        if (originalUserHome == null) {
            System.clearProperty("user.home");
        } else {
            System.setProperty("user.home", originalUserHome);
        }

        originalOsName = null;
        originalUserHome = null;
    }
}
