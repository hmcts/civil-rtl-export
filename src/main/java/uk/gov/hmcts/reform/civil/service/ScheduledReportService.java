package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

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
            log.debug("Finding judgments for all serviceIds: asOf [{}], rerun [{}], test [{}]", asOf, rerun, test);
            List<Judgment> judgments = judgmentRepository.findForUpdate(rerun, asOf);

            if (judgments.isEmpty()) {
                log.info("No judgments found for any serviceId");
            } else {
                // Separate judgments out into separate lists by service id
                Map<String, List<Judgment>> judgmentsGroupedByServiceId =
                    judgments.stream().collect(groupingBy(Judgment::getServiceId));

                for (Map.Entry<String, List<Judgment>> judgmentsServiceId : judgmentsGroupedByServiceId.entrySet()) {
                    processJudgments(judgmentsServiceId.getValue(), judgmentsServiceId.getKey(), asOf, rerun, test);
                }
            }

        } else {
            log.debug("Finding judgments for serviceId [{}]: asOf [{}], rerun [{}], test [{}]",
                      serviceId, asOf, rerun, test);
            List<Judgment> judgments = judgmentRepository.findForUpdateByServiceId(rerun, asOf, serviceId);

            if (judgments.isEmpty()) {
                log.info("No judgments found for serviceId [{}]", serviceId);
            } else {
                processJudgments(judgments, serviceId, asOf, rerun, test);
            }
        }

        log.info("Report generation completed");
    }

    private void processJudgments(List<Judgment> judgments,
                                  String serviceId,
                                  LocalDateTime asOf,
                                  boolean rerun,
                                  boolean test) {
        log.debug("Processing judgments: serviceId [{}], asOf [{}], rerun [{}], test [{}]",
                  serviceId, asOf, rerun, test);

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, serviceId, test);

        if (!test && !rerun) {
            updateReportedToRtl(judgments, asOf);
        }
    }

    /**
     * Updates the reported_to_rtl field for the processed judgments.
     *
     * @param judgments - The list of judgments to update
     * @param asOf - The timestamp to set for the reported_to_rtl field
     */
    private void updateReportedToRtl(List<Judgment> judgments, LocalDateTime asOf) {
        log.debug("Updating [{}] judgments with reported to RTL date [{}]", judgments.size(), asOf);

        judgments.forEach(judgment -> judgment.setReportedToRtl(asOf));
        judgmentRepository.saveAll(judgments);

        log.debug("Successfully updated [{}] judgments with reported to RTL date [{}]", judgments.size(), asOf);
    }
}
