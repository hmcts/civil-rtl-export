package uk.gov.hmcts.reform.civil.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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

    private static final String REGEX_VALID_SERVICE_ID = "^[A-Z0-9]{4}$";

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
    @Operation(summary = "Generate a report containing judgment data and send it via SFTP to RTL")
    @ApiResponse(responseCode = "200", description = "Report generated and sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid service id")
    @ApiResponse(responseCode = "500", description = "An error occurred during the report generation process")
    public ResponseEntity<String> triggerReport(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
        @RequestParam(defaultValue = "false") boolean test,
        @Valid @Pattern(regexp = REGEX_VALID_SERVICE_ID, message = "Invalid service id")
        @RequestParam(required = false) String serviceId) {

        try {
            log.info("Report requested. Test mode: [{}], asOf: [{}], Service ID: [{}]", test, asOf, serviceId);
            scheduledReportService.generateReport(test, asOf, serviceId);
            log.info("Report completed. Test mode: [{}], asOf: [{}], Service ID: [{}]", test, asOf, serviceId);

            return ResponseEntity.ok("Report has completed successfully");
        } catch (Exception e) {
            log.error("Error occurred during report generation. Test mode: [{}], asOf: [{}], Service ID: [{}]",
                      test, asOf, serviceId, e);
            return ResponseEntity.internalServerError().body("Report failed");
        }
    }
}
