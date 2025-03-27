package uk.gov.hmcts.reform.civil.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.HousekeepingService;

@Component
@Slf4j
public class HousekeepingTask implements Runnable {

    private final HousekeepingService housekeepingService;

    @Autowired
    public HousekeepingTask(HousekeepingService housekeepingService) {
        this.housekeepingService = housekeepingService;
    }

    @Override
    public void run() {
        log.info("Scheduled deletion of old judgments - started");
        housekeepingService.deleteOldJudgments();
        log.info("Scheduled deletion of old judgments - completed");
    }
}
