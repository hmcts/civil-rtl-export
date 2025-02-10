package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
public class HousekeepingService {

    private final JudgmentRepository judgmentRepository;
    private final int minimumAge;

    public HousekeepingService(JudgmentRepository judgmentRepository,
                               @Value("${rtl-export.task.housekeeping.minimumAge}") int minimumAge) {
        this.judgmentRepository = judgmentRepository;
        this.minimumAge = minimumAge;
    }

    public void deleteOldJudgments() {
        log.info("Deleting judgments older than [{}] days", minimumAge);

        LocalDateTime dateOfDeletion = LocalDate.now().minusDays(minimumAge).atStartOfDay();
        int deletedJudgmentsCount = judgmentRepository.deleteJudgmentsBefore(dateOfDeletion);

        log.info("[{}] old judgment(s) have been deleted", deletedJudgmentsCount);
    }
}
