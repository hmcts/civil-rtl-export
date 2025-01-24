package uk.gov.hmcts.reform.civil.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;

/**
 * ReportController handles the report scheduling service
 * "The Scheduled reporting service should be
 * established as an AKS scheduled process that can also be invoked directly
 * (with additional parameters)."
 */
@RestController
public class ReportController {

    //Auto wiring ScheduledReportService to handle report generation logic
    private final ScheduledReportService scheduledReportService;

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    public ReportController(ScheduledReportService scheduledReportService) {
        this.scheduledReportService = scheduledReportService;
    }


    /**
     * Controller method  to trigger report process
     * "The following parameters are accepted: asOf, test, serviceId".
     *
     * @param asOf 'asOf' parameter used to specify timestamp for rerun
     * @param test 'test' parameter to indicate if the report is being run in test mode or not
     * @param serviceId 'serviceId' to filter the report to a specific service id
     *
     * @return ResponseEntity with the result of the report execution.
     */


    //method is mapped to the HTTP GET request using @GetMapping annotation
    //when GET request sent to the "/trigger-report" URL this method is called
    @GetMapping("/trigger-report")
    public ResponseEntity<String> triggerReport(
        // @RequestParam annotation binds the value of the query parameter to the method argument
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime asOf,
        //asOf timestamp in the format 'YYYY-MM-DDTHH:MM:SS.ssZ'
        // if set to true, "in test mode - file is created but not copied to BAIS and the database is not updated"
        @RequestParam(defaultValue = "false") boolean test,
        //"for a specified service Id - handy if the process failed for specific service(s)"
        @RequestParam(required = false) String serviceId) {

        //logging the start of the process with test mode
        // "Write to log to say the processing has started"
        logger.info("Processing has started. Test mode: {}, asOf: {}, ServiceID: {}", test, asOf, serviceId);

        //calls service layer to handle the report generation logic
        try {
            //pass the provided parameters to the report service
            scheduledReportService.generateReport(test, asOf, serviceId);

            //if the process completes successfully returns this response to indicate process completed successfully
            // "Write to log to say processing has completed for serviceId"
            return ResponseEntity.ok("Processing has completed successfully for serviceId: " + serviceId);
        } catch (Exception e) {
            //handle any exceptions during the report generation process with a 500 status
            logger.error("Error occurred while processing. "
                             + "Test mode: {}, asOf: {}, Service ID: {}, Error: {}",
                            test, asOf, serviceId, e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}
