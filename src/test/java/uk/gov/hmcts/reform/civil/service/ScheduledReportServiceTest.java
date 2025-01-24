package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledReportServiceTest {

    private ScheduledReportService scheduledReportService;
    @Mock // Creates a mock instance of JudgmentRepository
    private JudgmentRepository judgmentRepository;
    @Mock // Creates a mock instance of JudgmentFileService
    private JudgmentFileService judgmentFileService;

    @BeforeEach // This method will run before each test case
    void setUp() {
        scheduledReportService = new ScheduledReportService(judgmentRepository, judgmentFileService);
    }

    @Test
    void shouldNotProcessWhenNoActiveServiceIdsFound() {
        when(judgmentRepository.findActiveServiceIds()).thenReturn(Collections.emptyList());

        boolean test = false;
        LocalDateTime asOf = null;
        String serviceId = null;

        scheduledReportService.generateReport(test, asOf, serviceId);

        verify(judgmentRepository).findActiveServiceIds();
        verifyNoMoreInteractions(judgmentFileService);

    }

    @Test
    void shouldProcessSpecificServiceId() {
        String serviceId = "serviceId1";
        List<Judgment> judgments = List.of(new Judgment(), new Judgment());
        LocalDateTime asOf = LocalDateTime.now();

        when(judgmentRepository.findForUpdate(true, asOf, serviceId)).thenReturn(judgments);

        boolean test = false;

        scheduledReportService.generateReport(test, asOf, serviceId);

        //Asserting that judgments are passed to judgment file service after processing
        verify(judgmentFileService).createAndSendJudgmentFile(judgments, asOf, serviceId, test);
    }

    @Test
    void shouldHandleNoJudgmentsForSpecificServiceId() {
        //Mocking empty list of judgments for a specific service ID
        String serviceId = "serviceId1";
        List<Judgment> judgments = Collections.emptyList();
        LocalDateTime asOf = LocalDateTime.now();

        when(judgmentRepository.findForUpdate(true, asOf, serviceId)).thenReturn(judgments);

        boolean test = false;

        scheduledReportService.generateReport(test, asOf, serviceId);

        verify(judgmentFileService, never()).createAndSendJudgmentFile(judgments, asOf, serviceId, test);
    }


    @Test
    void shouldUpdateRepositoryWhenNotInTestOrRerunMode() {
        String serviceId = "serviceId";
        List<Judgment> judgments = List.of(mock(Judgment.class), mock(Judgment.class));
        LocalDateTime asOf = LocalDateTime.now();
        //Mocking the find for update method when rerun is false and test is false
        when(judgmentRepository.findForUpdate(eq(false), any(LocalDateTime.class),
                                              eq(serviceId))).thenReturn(judgments);

        //Ensuring that both the test and rerun are false when updating the repository
        scheduledReportService.generateReport(false, null, serviceId);

        //Verifying the update in the repository
        verify(judgmentRepository).saveAll(judgments);
    }

    @Test
    void shouldNotUpdateDatabaseInTestMode() {
        String serviceId = "serviceId1";
        List<Judgment> judgments = List.of(new Judgment(), new Judgment());

        when(judgmentRepository.findForUpdate(eq(false), any(LocalDateTime.class), eq(serviceId)))
            .thenReturn(judgments);

        final boolean test = true;
        scheduledReportService.generateReport(true, null, serviceId);

        //Verifying the database does not update in test mode
        verify(judgmentRepository, never()).saveAll(any());
        verify(judgmentFileService).createAndSendJudgmentFile(eq(judgments), any(LocalDateTime.class), eq(serviceId),
                                                              eq(test));
    }

    @Test
    void testUpdateReportedToRtl() {
        Judgment judgment = new Judgment();
        List<Judgment> judgments = new ArrayList<>();
        judgments.add(judgment);

        String serviceId = "serviceID1";
        boolean test = false;

        when(judgmentRepository.findForUpdate(eq(false), any(LocalDateTime.class), eq(serviceId)))
            .thenReturn(judgments);

        scheduledReportService.generateReport(test, null, serviceId);

        // Verify that the judgment's reported_to_rtl is updated correctly
        assertNotNull(judgment.getReportedToRtl(), "The reportedToRtl field should be set");
        verify(judgmentRepository).saveAll(judgments);
    }
}
