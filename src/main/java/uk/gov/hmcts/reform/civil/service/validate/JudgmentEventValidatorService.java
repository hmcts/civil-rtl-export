package uk.gov.hmcts.reform.civil.service.validate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exception.MissingCancellationDateException;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedServiceIdException;
import uk.gov.hmcts.reform.civil.model.RegistrationType;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class JudgmentEventValidatorService {

    private final List<String> serviceIds;

    @Autowired
    public JudgmentEventValidatorService(ServiceIdConfigProperties configProperties) {
        serviceIds = configProperties.getServiceIds();
    }

    public void validateServiceId(String serviceId) {
        if (!serviceIds.contains(serviceId)) {
            log.debug("Unrecognised serviceId [{}]", serviceId);
            throw new UnrecognisedServiceIdException();
        }
    }

    public void validateCancellationDate(RegistrationType registrationType, LocalDate cancellationDate) {
        String regType = registrationType.getRegType();
        if ((RegistrationType.JUDGMENT_CANCELLED.getRegType().equals(regType)
            || RegistrationType.JUDGMENT_SATISFIED.getRegType().equals(regType)
            || RegistrationType.ADMIN_ORDER_REVOKED.getRegType().equals(regType))
            && cancellationDate == null) {
            log.debug("CancellationDate missing for registrationType [{}]", regType);
            throw new MissingCancellationDateException();
        }
    }
}
