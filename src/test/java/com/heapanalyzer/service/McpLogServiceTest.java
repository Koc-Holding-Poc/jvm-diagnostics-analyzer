package com.heapanalyzer.service;

import com.heapanalyzer.model.McpLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class McpLogServiceTest {

    private McpLogService service;

    @Mock
    private SseEmitter healthyEmitter;

    @Mock
    private SseEmitter failingEmitter;

    @BeforeEach
    void setUp() {
        service = new McpLogService();
    }

    @Test
    void subscribe_shouldRegisterEmitterForSession() {
        SseEmitter emitter = service.subscribe("session-1");

        List<SseEmitter> emitters = getSessionEmitters().get("session-1");

        assertEquals(1, emitters.size());
        assertSame(emitter, emitters.getFirst());
    }

    @Test
    void getRecentLogs_shouldReturnEmptyListWhenSessionHasNoLogs() {
        assertTrue(service.getRecentLogs("missing-session").isEmpty());
    }

    @Test
    void addLog_shouldKeepOnlyLatestFiveHundredEntries() {
        for (int i = 1; i <= 505; i++) {
            service.addLog("session-1", logEntry("log-" + i));
        }

        List<McpLogEntry> logs = service.getRecentLogs("session-1");

        assertEquals(500, logs.size());
        assertEquals("log-6", logs.getFirst().id());
        assertEquals("log-505", logs.getLast().id());
    }

    @Test
    void addLog_shouldRemoveEmittersThatFailDuringBroadcast() throws IOException {
        doThrow(new IOException("broken connection")).when(failingEmitter).send(any(SseEmitter.SseEventBuilder.class));
        getSessionEmitters().put("session-1", new java.util.concurrent.CopyOnWriteArrayList<>(List.of(healthyEmitter, failingEmitter)));

        service.addLog("session-1", logEntry("log-1"));

        List<SseEmitter> emitters = getSessionEmitters().get("session-1");

        verify(healthyEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(failingEmitter).send(any(SseEmitter.SseEventBuilder.class));
        assertEquals(1, emitters.size());
        assertSame(healthyEmitter, emitters.getFirst());
    }

    @Test
    void clearSession_shouldCompleteEmittersAndRemoveStoredState() throws IOException {
        service.addLog("session-1", logEntry("log-1"));
        getSessionEmitters().put("session-1", new java.util.concurrent.CopyOnWriteArrayList<>(List.of(healthyEmitter)));

        service.clearSession("session-1");

        verify(healthyEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(healthyEmitter).complete();
        assertTrue(service.getRecentLogs("session-1").isEmpty());
        assertFalse(getSessionEmitters().containsKey("session-1"));
    }

    private McpLogEntry logEntry(String id) {
        return new McpLogEntry(id, "REQUEST", Instant.parse("2024-01-01T00:00:00Z"), "payload-" + id, null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<SseEmitter>> getSessionEmitters() {
        return (Map<String, List<SseEmitter>>) ReflectionTestUtils.getField(service, "sessionEmitters");
    }
}
