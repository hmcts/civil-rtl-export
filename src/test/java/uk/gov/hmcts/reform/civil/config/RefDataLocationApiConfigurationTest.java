package uk.gov.hmcts.reform.civil.config;

import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.client.RefDataLocationApiErrorDecoder;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RefDataLocationApiConfigurationTest {

    private RefDataLocationApiConfiguration refDataLocationApiConfiguration;

    @BeforeEach
    void setUp() {
        refDataLocationApiConfiguration = new RefDataLocationApiConfiguration();
    }

    @Test
    void testErrorDecoder() {
        ErrorDecoder errorDecoder = refDataLocationApiConfiguration.errorDecoder();

        assertNotNull(errorDecoder, "ErrorDecoder should not be null");
        assertInstanceOf(RefDataLocationApiErrorDecoder.class,
                         errorDecoder,
                         "A RefDataLocationApiErrorDecoder should be returned");
    }
}
