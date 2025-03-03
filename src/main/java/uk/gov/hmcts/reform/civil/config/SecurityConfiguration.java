package uk.gov.hmcts.reform.civil.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final String[] SERVICE_AUTH_EXCLUDE_LIST = {
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

    private final ServiceAuthFilter serviceAuthFilter;

    @Autowired
    public SecurityConfiguration(ServiceAuthFilter serviceAuthFilter) {
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain serviceAuthExcludeFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(SERVICE_AUTH_EXCLUDE_LIST)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(serviceAuthFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
