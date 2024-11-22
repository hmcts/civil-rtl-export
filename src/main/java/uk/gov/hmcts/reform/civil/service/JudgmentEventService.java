package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.model.RegistrationType;
import uk.gov.hmcts.reform.civil.service.validate.JudgmentEventValidatorService;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class JudgmentEventService {

    private final JudgmentEventValidatorService judgmentEventValidatorService;
    private final RefDataService refDataService;
    private final JudgmentEventTransformerService judgmentEventTransformerService;

    @Autowired
    public JudgmentEventService(JudgmentEventValidatorService judgmentEventValidatorService,
                                RefDataService refDataService,
                                JudgmentEventTransformerService judgmentEventTransformerService) {
        this.judgmentEventValidatorService = judgmentEventValidatorService;
        this.refDataService = refDataService;
        this.judgmentEventTransformerService = judgmentEventTransformerService;
    }

    public void processJudgmentEvent(JudgmentEvent judgmentEvent) {
        log.debug("Process judgmentEvent: serviceId [{}], judgmentId [{}], timestamp [{}], caseNumber [{}]",
                  judgmentEvent.getServiceId(),
                  judgmentEvent.getJudgmentId(),
                  judgmentEvent.getJudgmentEventTimeStamp(),
                  judgmentEvent.getCaseNumber());

        validateJudgmentEvent(judgmentEvent);

        String courtLocationCode = getCourtLocationCode(judgmentEvent.getCourtEpimsId());
        List<Judgment> judgments = transformJudgmentEvent(judgmentEvent, courtLocationCode);

        persistJudgments(judgments);
    }

    private void validateJudgmentEvent(JudgmentEvent judgmentEvent) {
        String serviceId = judgmentEvent.getServiceId();
        log.debug("Validate serviceId [{}]", serviceId);
        judgmentEventValidatorService.validateServiceId(serviceId);

        RegistrationType regType = judgmentEvent.getRegistrationType();
        LocalDate cancellationDate = judgmentEvent.getCancellationDate();
        log.debug("Validate cancellationDate for registrationType [{}]", regType.getRegType());
        judgmentEventValidatorService.validateCancellationDate(regType, cancellationDate);
    }

    private String getCourtLocationCode(String courtEpimsId) {
        log.debug("Get courtLocationCode for epimsId [{}]", courtEpimsId);
        return refDataService.getCourtLocationCode(courtEpimsId);
    }

    private List<Judgment> transformJudgmentEvent(JudgmentEvent judgmentEvent, String courtLocationCode) {
        log.debug("Transform judgmentEvent: serviceId [{}], judgmentId [{}], timestamp [{}], caseNumber [{}]",
                  judgmentEvent.getServiceId(),
                  judgmentEvent.getJudgmentId(),
                  judgmentEvent.getJudgmentEventTimeStamp(),
                  judgmentEvent.getCaseNumber());

        return judgmentEventTransformerService.transformJudgmentEvent(judgmentEvent, courtLocationCode);
    }

    private void persistJudgments(List<Judgment> judgments) {
        // TODO: complete
    }
}
