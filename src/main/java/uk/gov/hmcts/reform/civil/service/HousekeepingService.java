package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
Housekeeping
A housekeeping process will be developed to manage the judgment event data.

The process will be scheduled to run once a week.
It will delete judgment events where the "reported to RTL" date is more than a specified number of days old.
The number of days will be configurable but will initially be set to 90.

The process will ignore judgment events that have not been sent to RTL (i.e. their "reported to RTL" date is null).

A JPA query will be used to delete the judgment events.  The SQL equivalent of the query will be:

DELETE
FROM   judgments
WHERE  reported_to_rtl < NOW() - INTERVAL '90 DAY';
The housekeeping process will be set up as a Spring scheduled task.

 */
@Service
@Slf4j
public class HousekeepingService {

    private final JudgmentRepository judgmentRepository;
    private final int minimumAge;

    public HousekeepingService(JudgmentRepository judgmentRepository,
                               @Value("${rtl-export.housekeeping.minimumAge}")
                               int minimumAge) {

        this.judgmentRepository = judgmentRepository;
        this.minimumAge = minimumAge;
    }

    //The housekeeping service allowing for deletions of old judgment events reported to the RTL feed
    //These events will be scheduled for deletion after the configurable deletion date set
    @Scheduled(cron = "${rtl-export.housekeeping.cron}")
    public void deleteOldJudgments() {
        LocalDateTime dateOfDeletion = LocalDate.now().minusDays(minimumAge).atStartOfDay();

        int deletedJudgmentsCount = judgmentRepository.deleteJudgmentsBefore(dateOfDeletion);
        log.info("{} old judgments have been deleted for housekeeping.", deletedJudgmentsCount);
    }
}
