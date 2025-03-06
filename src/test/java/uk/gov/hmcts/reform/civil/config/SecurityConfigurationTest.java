package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {

    @Mock
    private ServiceAuthFilter mockServiceAuthFilter;

    @Mock
    private HttpSecurity mockHttpSecurity;

    @Mock
    private DefaultSecurityFilterChain mockSecurityFilterChain;

    private SecurityConfiguration securityConfiguration;

    @BeforeEach
    void setUp() {
        securityConfiguration = new SecurityConfiguration(mockServiceAuthFilter);
    }

    @Test
    void testServiceAuthExcludeFilterChain() throws Exception {
        String[] authExcludeList = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/health",
            "/health/liveness",
            "/health/readiness",
            "/info",
            "/"
        };

        when(mockHttpSecurity.securityMatcher(authExcludeList)).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.authorizeHttpRequests(any())).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.build()).thenReturn(mockSecurityFilterChain);

        SecurityFilterChain filterChain = securityConfiguration.serviceAuthExcludeFilterChain(mockHttpSecurity);

        assertSame(mockSecurityFilterChain, filterChain, "Unexpected SecurityFilterChain returned");

        verify(mockHttpSecurity).securityMatcher(authExcludeList);
        verify(mockHttpSecurity).authorizeHttpRequests(any());
        verify(mockHttpSecurity).build();
    }

    @Test
    void testFilterChain() throws Exception {
        when(mockHttpSecurity.addFilterBefore(mockServiceAuthFilter, AbstractPreAuthenticatedProcessingFilter.class))
            .thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.authorizeHttpRequests(any())).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.sessionManagement(any())).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.httpBasic(any())).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.formLogin(any())).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.logout(any())).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.csrf(any())).thenReturn(mockHttpSecurity);
        when(mockHttpSecurity.build()).thenReturn(mockSecurityFilterChain);

        SecurityFilterChain filterChain = securityConfiguration.filterChain(mockHttpSecurity);

        assertSame(mockSecurityFilterChain, filterChain, "Unexpected SecurityFilterChain returned");

        verify(mockHttpSecurity).addFilterBefore(mockServiceAuthFilter, AbstractPreAuthenticatedProcessingFilter.class);
        verify(mockHttpSecurity).authorizeHttpRequests(any());
        verify(mockHttpSecurity).sessionManagement(any());
        verify(mockHttpSecurity).httpBasic(any());
        verify(mockHttpSecurity).formLogin(any());
        verify(mockHttpSecurity).logout(any());
        verify(mockHttpSecurity).csrf(any());
        verify(mockHttpSecurity).build();
    }
}
