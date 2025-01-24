package uk.gov.hmcts.reform.civil.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;

@RestController
@Slf4j
public class ReportController {

    private final ScheduledReportService scheduledReportService;

    @Autowired
    public ReportController(ScheduledReportService scheduledReportService) {
        this.scheduledReportService = scheduledReportService;
    }

    /**
     * Controller method  to trigger report process.
     *
     * @param asOf used to specify timestamp for rerun
     * @param test used to indicate if the report is being run in test mode or not
     * @param serviceId used to filter the report to a specific service id
     *
     * @return ResponseEntity with the result of the report execution.
     */
    @GetMapping("/trigger-report")
    public ResponseEntity<String> triggerReport(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
        @RequestParam(defaultValue = "false") boolean test,
        @RequestParam(required = false) String serviceId) {

        log.info("Processing has started. Test mode: {}, asOf: {}, ServiceID: {}", test, asOf, serviceId);

        try {
            scheduledReportService.generateReport(test, asOf, serviceId);
            return ResponseEntity.ok("Processing has completed successfully for serviceId: " + serviceId);
        } catch (Exception e) {
            log.error("Error occurred while processing. Test mode: {}, asOf: {}, Service ID: {}, Error: {}",
                      test, asOf, serviceId, e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}
