package com.heapanalyzer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MatAnalysisServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void resolveParseScript_windowsPrefersBat() throws Exception {
        MatAnalysisService service = new MatAnalysisService("/opt/mat", 30, "", "8g", mock(MatDownloadService.class));
        Path bat = tempDir.resolve("ParseHeapDump.bat");
        Files.writeString(bat, "echo test");
        Files.writeString(tempDir.resolve("ParseHeapDump.sh"), "echo test");

        String originalOs = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 11");

            Path resolved = service.resolveParseScript(tempDir);

            assertNotNull(resolved);
            assertEquals(bat, resolved);
        } finally {
            restoreOsName(originalOs);
        }
    }

    @Test
    void resolveParseScript_linuxUsesSh() throws Exception {
        MatAnalysisService service = new MatAnalysisService("/opt/mat", 30, "", "8g", mock(MatDownloadService.class));
        Path sh = tempDir.resolve("ParseHeapDump.sh");
        Files.writeString(sh, "echo test");

        String originalOs = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Linux");

            Path resolved = service.resolveParseScript(tempDir);

            assertNotNull(resolved);
            assertEquals(sh, resolved);
        } finally {
            restoreOsName(originalOs);
        }
    }

    @Test
    void buildMatProcessBuilder_windowsBatWrapsWithCmd() {
        MatAnalysisService service = new MatAnalysisService("/opt/mat", 30, "", "8g", mock(MatDownloadService.class));
        Path heapDump = tempDir.resolve("sample.hprof");
        Path bat = tempDir.resolve("ParseHeapDump.bat");

        String originalOs = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows Server 2022");

            ProcessBuilder pb = service.buildMatProcessBuilder(bat, heapDump, "-vmargs", "-Xmx2g");

            assertTrue(pb.command().size() >= 8);
            assertEquals("cmd", pb.command().get(0));
            assertEquals("/c", pb.command().get(1));
            assertEquals(bat.toString(), pb.command().get(2));
        } finally {
            restoreOsName(originalOs);
        }
    }

    private void restoreOsName(String originalOs) {
        if (originalOs == null) {
            System.clearProperty("os.name");
            return;
        }
        System.setProperty("os.name", originalOs);
    }
}
