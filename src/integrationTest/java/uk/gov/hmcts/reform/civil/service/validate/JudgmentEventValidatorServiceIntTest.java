package uk.gov.hmcts.reform.civil.service.validate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedServiceIdException;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    initializers = ConfigDataApplicationContextInitializer.class,
    classes = JudgmentEventValidatorService.class
)
@EnableConfigurationProperties(ServiceIdConfigProperties.class)
@ActiveProfiles("itest")
class JudgmentEventValidatorServiceIntTest {

    private static final String VALID_SERVICE_ID = "IT02";
    private static final String INVALID_SERVICE_ID = "ITX1";

    private final JudgmentEventValidatorService judgmentEventValidatorService;

    @Autowired
    public JudgmentEventValidatorServiceIntTest(JudgmentEventValidatorService judgmentEventValidatorService) {
        this.judgmentEventValidatorService = judgmentEventValidatorService;
    }

    @Test
    void testValidServiceId() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();
        judgmentEvent.setServiceId(VALID_SERVICE_ID);

        assertDoesNotThrow(() -> judgmentEventValidatorService.validateServiceId(judgmentEvent.getServiceId()),
                           "ServiceId should be valid");
    }

    @Test
    void testInvalidServiceId() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();
        judgmentEvent.setServiceId(INVALID_SERVICE_ID);

        assertThrows(UnrecognisedServiceIdException.class,
                     () -> judgmentEventValidatorService.validateServiceId(judgmentEvent.getServiceId()),
                     "ServiceId should be invalid");
    }
}
