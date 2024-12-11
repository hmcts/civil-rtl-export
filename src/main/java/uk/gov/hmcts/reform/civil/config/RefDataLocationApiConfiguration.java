package uk.gov.hmcts.reform.civil.config;

import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.civil.client.RefDataLocationApi;
import uk.gov.hmcts.reform.civil.client.RefDataLocationApiErrorDecoder;

@Configuration
@EnableFeignClients(clients = {RefDataLocationApi.class})
public class RefDataLocationApiConfiguration {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new RefDataLocationApiErrorDecoder();
    }
}
