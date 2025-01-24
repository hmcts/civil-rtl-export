package uk.gov.hmcts.reform.civil.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
//report service class allowing for report generation
public class ScheduledReportService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledReportService.class);

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
        //If asOf is null, do not rerun and use current time
        if (asOf == null) {
            rerun = false;
            asOf = LocalDateTime.now();
        } else {
            rerun = true;
        }

        //logging that report generation process has begun
        logger.info("Report generation has started");

        //if serviceId is null, find all the active serviceIds that have unreported judgments
        if (serviceId == null) {
            //fetches all distinct serviceIds with unreported judgments
            List<String> activeServiceIds = judgmentRepository.findActiveServiceIds();

            //When no active service Ids are found
            if (activeServiceIds.isEmpty()) {
                logger.warn("No active service IDs were found for processing");
                return;
            }

            //Process for each activeServiceID in the list
            for (String activeServiceId : activeServiceIds) {
                //logs the start of processing for each serviceId
                logger.info("Processing serviceId: {}, asOf: {}, rerun: {}, test: {}",
                            activeServiceId, asOf, rerun, test);
                //processing judgments for each serviceId
                processServiceId(activeServiceId, asOf, rerun, test);
            }
        } else {
            //logs processing for the specific serviceId
            logger.info("Processing specific serviceId: {}, asOf: {}, rerun: {}, test: {}",
                        serviceId, asOf, rerun, test);
            //processes judgments for the specific serviceId
            processServiceId(serviceId, asOf, rerun, test);
        }
        //log that the report generation process is complete
        logger.info("Report generation completed");
    }

    //"On invocation the report should: Check to see if additional parameters are provided".
    //checking whether asOf, serviceId, or test parameters were provided when the method was called

    /**
     * Processes judgments for a specific serviceId.
     *
     * @param serviceId - The serviceId to process
     * @param asOf - The timestamp to filter reports
     * @param rerun - Whether this is a rerun or not
     * @param test - Whether to run in test mode or not
     */
    private void processServiceId(String serviceId, LocalDateTime asOf, boolean rerun, boolean test) {
        logger.debug("Fetching judgments for serviceId: {}, asOf: {}, rerun:  {}", serviceId, asOf, rerun);

        //fetch judgments based on rerun and asOf conditions
        List<Judgment> judgments = judgmentRepository.findForUpdate(rerun, asOf, serviceId);

        if (judgments.isEmpty()) {
            logger.warn("No judgments found for serviceId: {}, asOf: {}, rerun: {}", serviceId, asOf, rerun);
            return;
        }

        String fileName = "judgments_" + asOf + "_" + serviceId + ".txt";
        logger.info("Writing judgments to file: {}", fileName);

        try {

            //Calling the creation and sending of file method from judgment file service
            judgmentFileService.createAndSendJudgmentFile(judgments, asOf, serviceId, test);

            //if not in test mode and not rerunning then update the database
            // log that the serviceId has been processed
            if (!test && !rerun) {
                //update the reported_to_rtl field in the database
                updateReportedToRtl(judgments, asOf);
                logger.info("Database has been updated for {} judgments for serviceId: {}",
                            judgments.size(), serviceId
                );
            }
        } catch (Exception e) {
            logger.error("Error processing serviceId: {}, Error: {}", serviceId, e.getMessage(), e);
        }
    }

    /**
     * Updates the reported_to_rtl field for the processed judgments.
     *
     * @param judgments - The list of judgments to update
     * @param asOf - The timestamp to set for the reported_to_rtl field
     */
    //Updates the reportedToRtl field to specified timestamp
    private void updateReportedToRtl(List<Judgment> judgments, LocalDateTime asOf) {
        if (asOf == null) {
            throw new IllegalStateException("asOf cannot be null when updating the reported_to_rtl field");
        }

        //For each judgment, the setReportedToRtl method is called with the asOf timestamp
        //This then updates the reportedToRtl field with the specified date and time
        judgments.forEach(judgment -> judgment.setReportedToRtl(asOf));
        try {
            //Saves updated judgments back to database
            judgmentRepository.saveAll(judgments);
            logger.debug("Successfully updated {} judgments with asOf: {}", judgments.size(), asOf);
        } catch (Exception e) {
            logger.error("Failed to update the reported_to_rtl field for judgments. Error: {}",
                         e.getMessage(), e);
        }
    }
}
