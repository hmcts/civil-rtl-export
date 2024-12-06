package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.exception.DifferentNumberOfDefendantsException;
import uk.gov.hmcts.reform.civil.exception.UpdateExistingJudgmentException;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.model.RegistrationType;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;
import uk.gov.hmcts.reform.civil.service.validate.JudgmentEventValidatorService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class JudgmentEventService {

    private final JudgmentEventValidatorService judgmentEventValidatorService;
    private final RefDataService refDataService;
    private final JudgmentEventTransformerService judgmentEventTransformerService;
    private final JudgmentRepository judgmentRepository;

    @Autowired
    public JudgmentEventService(JudgmentEventValidatorService judgmentEventValidatorService,
                                RefDataService refDataService,
                                JudgmentEventTransformerService judgmentEventTransformerService,
                                JudgmentRepository judgmentRepository) {
        this.judgmentEventValidatorService = judgmentEventValidatorService;
        this.refDataService = refDataService;
        this.judgmentEventTransformerService = judgmentEventTransformerService;
        this.judgmentRepository = judgmentRepository;
    }

    public void processJudgmentEvent(JudgmentEvent judgmentEvent) {
        log.debug("Process judgmentEvent: serviceId [{}], judgmentId [{}], timestamp [{}], caseNumber [{}]",
                  judgmentEvent.getServiceId(),
                  judgmentEvent.getJudgmentId(),
                  judgmentEvent.getJudgmentEventTimeStamp(),
                  judgmentEvent.getCaseNumber());

        validateJudgmentEvent(judgmentEvent);

        String courtLocationCode = getCourtLocationCode(judgmentEvent.getCourtEpimsId());

        persistJudgmentEvent(judgmentEvent, courtLocationCode);
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

    private void persistJudgmentEvent(JudgmentEvent judgmentEvent, String courtLocationCode) {
        String serviceId = judgmentEvent.getServiceId();
        String judgmentId = judgmentEvent.getJudgmentId();
        LocalDateTime judgmentEventTimestamp = judgmentEvent.getJudgmentEventTimeStamp();
        String caseNumber = judgmentEvent.getCaseNumber();

        log.debug("Persist judgmentEvent: serviceId [{}], judgmentId [{}], timestamp [{}], caseNumber [{}]",
                  serviceId,
                  judgmentId,
                  judgmentEventTimestamp,
                  caseNumber);

        List<Judgment> judgments = transformJudgmentEvent(judgmentEvent, courtLocationCode);

        List<Judgment> existingJudgments =
            judgmentRepository.findByEventDetails(serviceId, judgmentId, judgmentEventTimestamp, caseNumber);

        if (existingJudgments.isEmpty()) {
            log.debug("No existing judgment(s), save judgment event");
            judgmentRepository.saveAll(judgments);
        } else {
            log.debug("Existing judgment(s), check against judgment event");
            if (existingJudgments.size() == judgments.size()) {
                int index = 0;
                Judgment judgment;

                for (Judgment existingJudgment : existingJudgments) {
                    judgment = judgments.get(index);
                    if (!existingJudgment.equalsJudgment(judgment)) {
                        throw new UpdateExistingJudgmentException();
                    }
                    index++;
                }
                log.debug("Existing judgment(s) match judgment event");
            } else {
                throw new DifferentNumberOfDefendantsException();
            }
        }
    }
}
