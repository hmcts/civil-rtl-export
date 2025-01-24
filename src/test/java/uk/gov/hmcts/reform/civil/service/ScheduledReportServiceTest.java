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

    @Mock
    private JudgmentRepository judgmentRepository;

    @Mock
    private JudgmentFileService judgmentFileService;

    @BeforeEach
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

        verify(judgmentFileService).createAndSendJudgmentFile(judgments, asOf, serviceId, test);
    }

    @Test
    void shouldHandleNoJudgmentsForSpecificServiceId() {
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

        when(judgmentRepository.findForUpdate(eq(false), any(LocalDateTime.class), eq(serviceId)))
            .thenReturn(judgments);

        scheduledReportService.generateReport(false, null, serviceId);

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

        verify(judgmentRepository, never()).saveAll(any());
        verify(judgmentFileService)
            .createAndSendJudgmentFile(eq(judgments), any(LocalDateTime.class), eq(serviceId), eq(test));
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

        assertNotNull(judgment.getReportedToRtl(), "The reportedToRtl field should be set");
        verify(judgmentRepository).saveAll(judgments);
    }
}
