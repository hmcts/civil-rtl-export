package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ScheduledReportService {

    private final JudgmentRepository judgmentRepository;

    private final JudgmentFileService judgmentFileService;

    @Autowired
    public ScheduledReportService(JudgmentRepository judgmentRepository, JudgmentFileService judgmentFileService) {
        this.judgmentRepository = judgmentRepository;
        this.judgmentFileService = judgmentFileService;
    }

    /**
     * Main method to generate report.
     *
     * @param test - whether report is in test mode (no database updates or file transfers)
     * @param asOf - timestamp for existing reports
     * @param serviceId - id of the service to generate reports for
     */
    public void generateReport(boolean test, LocalDateTime asOf, String serviceId) {
        boolean rerun;

        if (asOf == null) {
            rerun = false;
            asOf = LocalDateTime.now();
        } else {
            rerun = true;
        }

        log.info("Report generation has started");
        if (serviceId == null) {

            List<String> activeServiceIds = judgmentRepository.findActiveServiceIds();

            if (activeServiceIds.isEmpty()) {
                log.warn("No active service IDs were found for processing");
                return;
            }

            for (String activeServiceId : activeServiceIds) {
                log.info("Processing serviceId: {}, asOf: {}, rerun: {}, test: {}", activeServiceId, asOf, rerun, test);
                processServiceId(activeServiceId, asOf, rerun, test);
            }
        } else {
            log.info("Processing specific serviceId: {}, asOf: {}, rerun: {}, test: {}", serviceId, asOf, rerun, test);
            processServiceId(serviceId, asOf, rerun, test);
        }
        log.info("Report generation completed");
    }

    /**
     * Processes judgments for a specific serviceId.
     *
     * @param serviceId - The serviceId to process
     * @param asOf - The timestamp to filter reports
     * @param rerun - Whether this is a rerun or not
     * @param test - Whether to run in test mode or not
     */
    private void processServiceId(String serviceId, LocalDateTime asOf, boolean rerun, boolean test) {
        log.debug("Fetching judgments for serviceId: {}, asOf: {}, rerun: {}", serviceId, asOf, rerun);

        List<Judgment> judgments = judgmentRepository.findForUpdate(rerun, asOf, serviceId);

        if (judgments.isEmpty()) {
            log.warn("No judgments found for serviceId: {}, asOf: {}, rerun: {}", serviceId, asOf, rerun);
            return;
        }

        String fileName = "judgments_" + asOf + "_" + serviceId + ".txt";
        log.info("Writing judgments to file: {}", fileName);

        try {
            judgmentFileService.createAndSendJudgmentFile(judgments, asOf, serviceId, test);

            if (!test && !rerun) {
                updateReportedToRtl(judgments, asOf);
                log.info("Database has been updated for {} judgments for serviceId: {}", judgments.size(), serviceId);
            }
        } catch (Exception e) {
            log.error("Error processing serviceId: {}, Error: {}", serviceId, e.getMessage(), e);
        }
    }

    /**
     * Updates the reported_to_rtl field for the processed judgments.
     *
     * @param judgments - The list of judgments to update
     * @param asOf - The timestamp to set for the reported_to_rtl field
     */
    private void updateReportedToRtl(List<Judgment> judgments, LocalDateTime asOf) {
        if (asOf == null) {
            throw new IllegalStateException("asOf cannot be null when updating the reported_to_rtl field");
        }

        judgments.forEach(judgment -> judgment.setReportedToRtl(asOf));
        try {
            //Saves updated judgments back to database
            judgmentRepository.saveAll(judgments);
            log.debug("Successfully updated {} judgments with asOf: {}", judgments.size(), asOf);
        } catch (Exception e) {
            log.error("Failed to update the reported_to_rtl field for judgments. Error: {}", e.getMessage(), e);
        }
    }
}
