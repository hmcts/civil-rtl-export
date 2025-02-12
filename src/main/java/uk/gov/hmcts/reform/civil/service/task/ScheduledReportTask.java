package uk.gov.hmcts.reform.civil.service.task;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class ScheduledReportTask implements Runnable {

    @Setter
    private String asOf;

    @Setter
    private boolean test;

    @Setter
    private String serviceId;

    private final ScheduledReportService scheduledReportService;

    @Autowired
    public ScheduledReportTask(ScheduledReportService scheduledReportService,
                               @Value("${rtl-export.task.scheduledReport.asOf}") String asOf,
                               @Value("${rtl-export.task.scheduledReport.test}") boolean test,
                               @Value("${rtl-export.task.scheduledReport.serviceId}") String serviceId) {
        this.scheduledReportService = scheduledReportService;
        this.asOf = asOf;
        this.test = test;
        this.serviceId = serviceId;
    }

    @Override
    public void run() {
        log.info("Scheduled report - started");

        // Convert blank asOf and serviceId property values to null
        LocalDateTime reportAsOf = asOf == null || asOf.isBlank() ? null :
            LocalDateTime.parse(asOf, DateTimeFormatter.ISO_DATE_TIME);
        String reportServiceId = serviceId == null || serviceId.isBlank() ? null : serviceId;

        log.info("Scheduled report - params: test [{}], asOf [{}], serviceId [{}]", test, reportAsOf, reportServiceId);
        scheduledReportService.generateReport(test, reportAsOf, reportServiceId);

        log.info("Scheduled report - completed");
    }
}
