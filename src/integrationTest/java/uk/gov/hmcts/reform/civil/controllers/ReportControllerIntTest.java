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

@ActiveProfiles("itest")
@WebMvcTest(ReportController.class)
class ReportControllerIntTest {

    private MockMvc mockMvc;

    @MockBean
    private ScheduledReportService scheduledReportService;

    @Autowired
    public ReportControllerIntTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void reportReturnsSuccessfulResponse() throws Exception {
        LocalDateTime asOf = LocalDateTime.of(2024, 10, 22, 12, 0);
        boolean test = true;
        String serviceId = "testServiceId";

        String url = String.format("/trigger-report?asOf=%s&test=%b&serviceId=%s",
                asOf.format(DateTimeFormatter.ISO_DATE_TIME), test, serviceId);

        Mockito.doNothing().when(scheduledReportService).generateReport(test, asOf, serviceId);

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Processing has completed successfully for serviceId: " + serviceId));

        Mockito.verify(scheduledReportService).generateReport(test, asOf, serviceId);
    }

    @Test
    void reportHandlesException() throws Exception {
        LocalDateTime asOf = LocalDateTime.of(2024, 10, 22, 12, 0);
        boolean test = true;
        String serviceId = "testServiceId";

        Mockito.doThrow(new RuntimeException("Report generation failed")).when(scheduledReportService)
                .generateReport(test, asOf, serviceId);

        String url = String.format("/trigger-report?asOf=%s&test=%b&serviceId=%s",
                asOf.format(DateTimeFormatter.ISO_DATE_TIME), test, serviceId);

        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) // Expect HTTP 500 error status
                .andExpect(content().string("An error occurred: Report generation failed"));
    }
}
