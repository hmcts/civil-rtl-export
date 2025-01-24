package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.civil.service.ScheduledReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Integration tests for ReportController class, testing functionality.
 */
@ActiveProfiles("test")
@WebMvcTest(ReportController.class) // Spring MVC test used for ReportController
public class ReportControllerIntTest {

    @Autowired
    private MockMvc mockMvc; // MockMvc defined for performing HTTP requests

    @MockBean
    private ScheduledReportService scheduledReportService; // ScheduledReportService defined for mockito tests



    // Test to verify a successful response from the controller
    @Test
    void reportReturnsSuccessfulResponse() throws Exception {
        // test parameters
        LocalDateTime asOf = LocalDateTime.of(2024, 10, 22, 12, 0); // asOf parameter
        boolean test = true; // test parameter set to true
        String serviceId = "testServiceId"; // serviceId parameter

        // Constructing request URL with query parameters
        String url = String.format("/trigger-report?asOf=%s&test=%b&serviceId=%s",
                asOf.format(DateTimeFormatter.ISO_DATE_TIME), test, serviceId);

        //Simulating generation of successful report
        Mockito.doNothing().when(scheduledReportService).generateReport(test, asOf, serviceId);

        // Perform a GET request to the controller and verify response
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Expect HTTP 200 OK status
                .andExpect(content().string("Processing has completed successfully for serviceId: "
                        + serviceId)); // Verify response content

        // Verify generateReport method in the service called with parameters
        Mockito.verify(scheduledReportService).generateReport(test, asOf, serviceId);
    }

    // Test to verify that the controller handles any exceptions and returns the appropriate response
    @Test
    void reportHandlesException() throws Exception {
        // Define test parameters
        LocalDateTime asOf = LocalDateTime.of(2024, 10, 22, 12, 0);
        boolean test = true;
        String serviceId = "testServiceId";

        // Triggers the generateReport method in the service to throw an exception if generation fails
        Mockito.doThrow(new RuntimeException("Report generation failed")).when(scheduledReportService)
                .generateReport(test, asOf, serviceId);

        // Constructs the request URL with the query parameters
        String url = String.format("/trigger-report?asOf=%s&test=%b&serviceId=%s",
                asOf.format(DateTimeFormatter.ISO_DATE_TIME), test, serviceId);

        // Performs GET request and verifies the response
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Expect HTTP 500 error status
                .andExpect(content().string("An error occurred: Report generation failed"));
    }
}


