package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledReportServiceTest {

    private static final boolean NOT_RERUN = false;
    private static final boolean IS_RERUN = true;
    private static final boolean NOT_TEST = false;
    private static final boolean IS_TEST = true;

    private static final String SERVICE_ID_1 = "UT01";
    private static final String SERVICE_ID_2 = "UT02";

    @Mock
    private JudgmentRepository judgmentRepository;

    @Mock
    private JudgmentFileService judgmentFileService;

    private ScheduledReportService scheduledReportService;

    @BeforeEach
    void setUp() {
        scheduledReportService = new ScheduledReportService(judgmentRepository, judgmentFileService);
    }

    @Test
    void testNoJudgmentsNoServiceId() {
        when(judgmentRepository.findForUpdate(NOT_RERUN, null)).thenReturn(Collections.emptyList());

        scheduledReportService.generateReport(NOT_TEST, null, null);

        verify(judgmentRepository).findForUpdate(NOT_RERUN, null);
        verify(judgmentFileService, never())
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), anyString(), anyBoolean());
        verify(judgmentRepository, never()).saveAll(anyList());
    }

    @Test
    void testNoJudgmentsServiceId() {
        when(judgmentRepository.findForUpdateByServiceId(NOT_RERUN, null, SERVICE_ID_1))
            .thenReturn(Collections.emptyList());

        scheduledReportService.generateReport(NOT_TEST, null, SERVICE_ID_1);

        verify(judgmentRepository).findForUpdateByServiceId(NOT_RERUN, null, SERVICE_ID_1);
        verify(judgmentFileService, never())
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), anyString(), anyBoolean());
        verify(judgmentRepository, never()).saveAll(anyList());
    }

    @Test
    void testJudgmentsNoServiceId() {
        List<Judgment> judgments = new ArrayList<>();

        Judgment judgment1 = new Judgment();
        judgment1.setServiceId(SERVICE_ID_1);
        judgments.add(judgment1);

        Judgment judgment2 = new Judgment();
        judgment2.setServiceId(SERVICE_ID_2);
        judgments.add(judgment2);

        when(judgmentRepository.findForUpdate(NOT_RERUN, null)).thenReturn(judgments);

        scheduledReportService.generateReport(NOT_TEST, null, null);

        assertNotNull(judgment1.getReportedToRtl(), "Judgment1 reported to RTL date should not be null");
        assertNotNull(judgment2.getReportedToRtl(), "Judgment2 reported to RTL date should not be null");

        verify(judgmentRepository).findForUpdate(NOT_RERUN, null);
        verify(judgmentFileService)
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_1), eq(NOT_TEST));
        verify(judgmentFileService)
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_2), eq(NOT_TEST));
        verify(judgmentRepository, times(2)).saveAll(anyList());
    }

    @Test
    void testJudgmentsServiceId() {
        List<Judgment> judgments = new ArrayList<>();

        Judgment judgment = new Judgment();
        judgment.setServiceId(SERVICE_ID_1);
        judgments.add(judgment);

        when(judgmentRepository.findForUpdateByServiceId(NOT_RERUN, null, SERVICE_ID_1)).thenReturn(judgments);

        scheduledReportService.generateReport(NOT_TEST, null, SERVICE_ID_1);

        assertNotNull(judgment.getReportedToRtl(), "Judgment reported to RTL date should not be null");

        verify(judgmentRepository).findForUpdateByServiceId(NOT_RERUN, null, SERVICE_ID_1);
        verify(judgmentFileService)
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_1), eq(NOT_TEST));
        verify(judgmentRepository).saveAll(anyList());
    }

    @ParameterizedTest
    @MethodSource("rerunOrTestParams")
    void testJudgmentsRerunOrTest(LocalDateTime asOf, boolean test) {
        List<Judgment> judgments = new ArrayList<>();

        Judgment judgment = new Judgment();
        judgment.setServiceId(SERVICE_ID_1);
        judgment.setReportedToRtl(asOf);
        judgments.add(judgment);

        if (asOf == null) {
            when(judgmentRepository.findForUpdate(NOT_RERUN, null)).thenReturn(judgments);
        } else {
            when(judgmentRepository.findForUpdate(IS_RERUN, asOf)).thenReturn(judgments);
        }

        scheduledReportService.generateReport(test, asOf, null);

        if (asOf == null) {
            assertNull(judgment.getReportedToRtl(), "Judgment reported to RTL date should be null");
            verify(judgmentRepository).findForUpdate(NOT_RERUN, null);
            verify(judgmentFileService)
                .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_1), eq(test));
        } else {
            assertEquals(asOf, judgment.getReportedToRtl(), "Judgment reported to RTL date should not be changed");
            verify(judgmentRepository).findForUpdate(IS_RERUN, asOf);
            verify(judgmentFileService).createAndSendJudgmentFile(anyList(), eq(asOf), eq(SERVICE_ID_1), eq(test));
        }
        verify(judgmentRepository, never()).saveAll(anyList());
    }

    private static Stream<Arguments> rerunOrTestParams() {
        return Stream.of(
            arguments(null, IS_TEST),
            arguments(LocalDateTime.of(2025, 1, 30, 10, 30, 0), NOT_TEST),
            arguments(LocalDateTime.of(2025, 1, 30, 15, 6, 1), IS_TEST)
        );
    }
}
