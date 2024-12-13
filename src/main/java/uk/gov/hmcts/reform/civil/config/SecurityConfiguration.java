package uk.gov.hmcts.reform.civil.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String[] ALLOWED_LIST = {
        "/health",
        "/"
    };

    private final ServiceAuthFilter serviceAuthFilter;

    @Autowired
    public SecurityConfiguration(ServiceAuthFilter serviceAuthFilter) {
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Bean
    public SecurityFilterChain allowedFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth.requestMatchers(ALLOWED_LIST).permitAll())
            .build();
    }

    // TODO: Finish this class
}
