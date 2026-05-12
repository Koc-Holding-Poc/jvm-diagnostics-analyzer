package com.heapanalyzer.config;

import com.heapanalyzer.service.ConfigService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetupInterceptorTest {

    @Mock
    private ConfigService configService;

    @ParameterizedTest
    @ValueSource(strings = {"/setup", "/api/settings", "/api/settings/prompts", "/css/app.css", "/js/app.js", "/img/logo.png", "/favicon.ico", "/error"})
    void preHandle_shouldAllowExcludedPathsWhenAppIsNotConfigured(String path) throws Exception {
        SetupInterceptor interceptor = new SetupInterceptor(configService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verifyNoInteractions(configService);
    }

    @Test
    void preHandle_shouldRedirectProtectedPathsToSetupWhenAppIsNotConfigured() throws Exception {
        SetupInterceptor interceptor = new SetupInterceptor(configService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(configService.isConfigured()).thenReturn(false);

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals("/setup", response.getRedirectedUrl());
    }

    @Test
    void preHandle_shouldAllowProtectedPathsWhenAppIsConfigured() throws Exception {
        SetupInterceptor interceptor = new SetupInterceptor(configService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(configService.isConfigured()).thenReturn(true);

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertNull(response.getRedirectedUrl());
    }
}
