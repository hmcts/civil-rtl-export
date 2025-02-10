package uk.gov.hmcts.reform.civil.service.task;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.service.HousekeepingService;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScheduledTaskRunner {

    public static final String TASK_NAME_SCHEDULED_REPORT = "SCHEDULED_REPORT";
    public static final String TASK_NAME_HOUSEKEEPING = "HOUSEKEEPING";

    @Value("${rtl-export.task.scheduledReport.asOf}")
    @Setter
    private String reportAsOf;

    @Value("${rtl-export.task.scheduledReport.test}")
    @Setter
    private Boolean reportTest;

    @Value("${rtl-export.task.scheduledReport.serviceId}")
    @Setter
    private String reportServiceId;

    private final ScheduledReportService scheduledReportService;

    private final HousekeepingService housekeepingService;

    @Autowired
    public ScheduledTaskRunner(ScheduledReportService scheduledReportService,
                               HousekeepingService housekeepingService) {
        this.scheduledReportService = scheduledReportService;
        this.housekeepingService = housekeepingService;
    }

    public void run(String taskName) {
        if (TASK_NAME_SCHEDULED_REPORT.equals(taskName)) {
            runScheduledReport();
        } else if (TASK_NAME_HOUSEKEEPING.equals(taskName)) {
            runHousekeeping();
        } else {
            log.error("Unknown task name [{}]", taskName);
        }
    }

    private void runScheduledReport() {
        log.info("Scheduled report - started");

        // Convert blank asOf and serviceId property values to null
        LocalDateTime asOf = reportAsOf == null || reportAsOf.isBlank() ? null :
            LocalDateTime.parse(reportAsOf, DateTimeFormatter.ISO_DATE_TIME);
        String serviceId = reportServiceId == null || reportServiceId.isBlank() ? null : reportServiceId;

        log.info("Scheduled report - params: test [{}], asOf [{}], serviceId [{}]", reportTest, asOf, serviceId);
        scheduledReportService.generateReport(reportTest, asOf, serviceId);

        log.info("Scheduled report - completed");
    }

    private void runHousekeeping() {
        log.info("Scheduled deletion of old judgments - started");
        housekeepingService.deleteOldJudgments();
        log.info("Scheduled deletion of old judgments - completed");
    }
}
