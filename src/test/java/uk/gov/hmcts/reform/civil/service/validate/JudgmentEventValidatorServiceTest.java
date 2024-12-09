package uk.gov.hmcts.reform.civil.service.validate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.civil.exception.MissingCancellationDateException;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedServiceIdException;
import uk.gov.hmcts.reform.civil.model.RegistrationType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JudgmentEventValidatorServiceTest {

    private static final String VALID_SERVICE_ID = "UT01";
    private static final String INVALID_SERVICE_ID = "UTX1";

    private JudgmentEventValidatorService judgmentEventValidatorService;

    @BeforeEach
    void setUp() {
        List<String> serviceIds = new ArrayList<>();
        serviceIds.add(VALID_SERVICE_ID);

        ServiceIdConfigProperties configProps = new ServiceIdConfigProperties();
        configProps.setServiceIds(serviceIds);

        judgmentEventValidatorService = new JudgmentEventValidatorService(configProps);
    }

    @Test
    void testServiceIdValid() {
        assertDoesNotThrow(() -> judgmentEventValidatorService.validateServiceId(VALID_SERVICE_ID),
                           "ServiceId should be valid");
    }

    @Test
    void testServiceIdInvalid() {
        assertThrows(UnrecognisedServiceIdException.class,
                     () -> judgmentEventValidatorService.validateServiceId(INVALID_SERVICE_ID),
                     "ServiceId should be invalid");
    }

    @ParameterizedTest
    @EnumSource(
        value = RegistrationType.class,
        names = {"JUDGMENT_CANCELLED", "JUDGMENT_SATISFIED", "ADMIN_ORDER_REVOKED"},
        mode = EnumSource.Mode.INCLUDE
    )
    void testCancellationDateSetWhenRequired(RegistrationType regType) {
        assertMissingCancellationDateExceptionNotThrown(regType, LocalDate.now());
    }

    @ParameterizedTest
    @EnumSource(
        value = RegistrationType.class,
        names = {"JUDGMENT_CANCELLED", "JUDGMENT_SATISFIED", "ADMIN_ORDER_REVOKED"},
        mode = EnumSource.Mode.INCLUDE
    )
    void testCancellationDateNotSetWhenRequired(RegistrationType regType) {
        assertThrows(
            MissingCancellationDateException.class,
            () -> judgmentEventValidatorService.validateCancellationDate(regType, null),
            "Missing cancellation date exception should be thrown");
    }

    @ParameterizedTest
    @EnumSource(
        value = RegistrationType.class,
        names = {"JUDGMENT_CANCELLED", "JUDGMENT_SATISFIED", "ADMIN_ORDER_REVOKED"},
        mode = EnumSource.Mode.EXCLUDE
    )
    void testCancellationDateSetWhenNotRequired(RegistrationType regType) {
        assertMissingCancellationDateExceptionNotThrown(regType, LocalDate.now());
    }

    @ParameterizedTest
    @EnumSource(
        value = RegistrationType.class,
        names = {"JUDGMENT_CANCELLED", "JUDGMENT_SATISFIED", "ADMIN_ORDER_REVOKED"},
        mode = EnumSource.Mode.EXCLUDE
    )
    void testCancellationDateNotSetWhenNotRequired(RegistrationType regType) {
        assertMissingCancellationDateExceptionNotThrown(regType, null);
    }

    private void assertMissingCancellationDateExceptionNotThrown(RegistrationType regType, LocalDate cancellationDate) {
        assertDoesNotThrow(() -> judgmentEventValidatorService.validateCancellationDate(regType, cancellationDate),
                           "Missing cancellation date exception should not be thrown");
    }
}
