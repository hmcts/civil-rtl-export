package uk.gov.hmcts.reform.civil.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("itest")
class SecurityConfigurationIntTest {

    private static final String URI_JUDGMENT = "/judgment";

    private final HttpSecurity httpSecurity;

    private final SecurityConfiguration securityConfiguration;

    @Autowired
    public SecurityConfigurationIntTest(SecurityConfiguration securityConfiguration, HttpSecurity httpSecurity) {
        this.securityConfiguration = securityConfiguration;
        this.httpSecurity = httpSecurity;
    }

    @ParameterizedTest
    @ValueSource(strings = {"/health", "/health/liveness", "/health/readiness", "/"})
    void testAuthExcludeFilterChainMatches(String uri) throws Exception {
        SecurityFilterChain filterChain = securityConfiguration.serviceAuthExcludeFilterChain(httpSecurity);

        assertSecurityFilterChainNotNull(filterChain);
        assertSecurityFilterChainMatches("GET", uri, filterChain);
    }

    @Test
    void testAuthExcludeFilterChainDoesNotMatch() throws Exception {
        SecurityFilterChain filterChain = securityConfiguration.serviceAuthExcludeFilterChain(httpSecurity);

        assertSecurityFilterChainNotNull(filterChain);

        HttpServletRequest req = new MockHttpServletRequest("POST", URI_JUDGMENT);
        assertFalse(filterChain.matches(req), "SecurityFilterChain should not match against URI " + URI_JUDGMENT);

    }

    @Test
    void testFilterChainMatches() throws Exception {
        SecurityFilterChain filterChain = securityConfiguration.filterChain(httpSecurity);

        assertSecurityFilterChainNotNull(filterChain);
        assertSecurityFilterChainMatches("POST", URI_JUDGMENT, filterChain);
    }

    private void assertSecurityFilterChainNotNull(SecurityFilterChain filterChain) {
        assertNotNull(filterChain, "SecurityFilterChain should not be null");
    }

    private void assertSecurityFilterChainMatches(String method, String uri, SecurityFilterChain filterChain) {
        HttpServletRequest req = new MockHttpServletRequest(method, uri);
        assertTrue(filterChain.matches(req), "SecurityFilterChain should match against URI " + uri);
    }
}
