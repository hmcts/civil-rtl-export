package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
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

    private static final boolean NOT_TEST = false;
    private static final boolean IS_TEST = true;

    private static final LocalDateTime DATE_TIME_AS_OF = LocalDateTime.of(2025, 2, 17, 18, 22, 4);

    private static final String SERVICE_ID_1 = "UT01";
    private static final String SERVICE_ID_2 = "UT02";

    @Mock
    private JudgmentRepository mockJudgmentRepository;

    @Mock
    private JudgmentFileService mockJudgmentFileService;

    private ScheduledReportService scheduledReportService;

    @BeforeEach
    void setUp() {
        scheduledReportService = new ScheduledReportService(mockJudgmentRepository, mockJudgmentFileService);
    }

    @ParameterizedTest
    @MethodSource("asOfServiceIdParams")
    void testNoJudgments(LocalDateTime asOf, String serviceId) {
        setUpExpectedFindForRtlBehaviour(asOf, serviceId, Collections.emptyList());

        scheduledReportService.generateReport(NOT_TEST, asOf, serviceId);

        verifyExpectedFindForRtlBehaviour(asOf, serviceId);
        verify(mockJudgmentFileService, never())
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), anyString(), anyBoolean());
        verify(mockJudgmentRepository, never()).saveAll(anyList());
    }

    @ParameterizedTest
    @MethodSource("asOfServiceIdParams")
    void testJudgments(LocalDateTime asOf, String serviceId) {
        Judgment judgment = createJudgment(SERVICE_ID_1, asOf);

        List<Judgment> judgments = new ArrayList<>();
        judgments.add(judgment);

        setUpExpectedFindForRtlBehaviour(asOf, serviceId, judgments);

        scheduledReportService.generateReport(NOT_TEST, asOf, serviceId);

        if (asOf == null) {
            assertNotNull(judgment.getReportedToRtl(), "Judgment reported to RTL date should not be null");
            verify(mockJudgmentFileService)
                .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_1), eq(false));
            verify(mockJudgmentRepository).saveAll(anyList());
        } else {
            assertEquals(asOf, judgment.getReportedToRtl(), "Judgment reported to RTL date should not be changed");
            verify(mockJudgmentFileService)
                .createAndSendJudgmentFile(anyList(), eq(asOf), eq(SERVICE_ID_1), eq(false));
            verify(mockJudgmentRepository, never()).saveAll(anyList());
        }

        verifyExpectedFindForRtlBehaviour(asOf, serviceId);
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("asOfParams")
    void testJudgmentsMultipleServiceIds(LocalDateTime asOf) {
        List<Judgment> judgments = new ArrayList<>();
        judgments.add(createJudgment(1L, SERVICE_ID_1));
        judgments.add(createJudgment(2L, SERVICE_ID_2));

        setUpExpectedFindForRtlBehaviour(asOf, null, judgments);

        scheduledReportService.generateReport(NOT_TEST, asOf, null);

        if (asOf == null) {
            for (Judgment judgment : judgments) {
                assertNotNull(judgment.getReportedToRtl(),
                              "Judgment " + judgment.getId() + " reported to RTL date should not be null");
            }
            verify(mockJudgmentFileService)
                .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_1), eq(NOT_TEST));
            verify(mockJudgmentFileService)
                .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_2), eq(NOT_TEST));
            verify(mockJudgmentRepository, times(2)).saveAll(anyList());
        } else {
            for (Judgment judgment : judgments) {
                assertEquals(DATE_TIME_AS_OF,
                             judgment.getReportedToRtl(),
                             "Judgment " + judgment.getId() + " reported to RTL date should not have changed");
            }
            verify(mockJudgmentFileService)
                .createAndSendJudgmentFile(anyList(), eq(DATE_TIME_AS_OF), eq(SERVICE_ID_1), eq(NOT_TEST));
            verify(mockJudgmentFileService)
                .createAndSendJudgmentFile(anyList(), eq(DATE_TIME_AS_OF), eq(SERVICE_ID_2), eq(NOT_TEST));
            verify(mockJudgmentRepository, never()).saveAll(anyList());
        }

        verifyExpectedFindForRtlBehaviour(asOf, null);
    }

    @ParameterizedTest
    @MethodSource("asOfServiceIdParams")
    void testNoJudgmentsTestMode(LocalDateTime asOf, String serviceId) {
        setUpExpectedFindForRtlBehaviour(asOf, serviceId, Collections.emptyList());

        scheduledReportService.generateReport(IS_TEST, asOf, serviceId);

        verifyExpectedFindForRtlBehaviour(asOf, serviceId);
        verify(mockJudgmentFileService, never())
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), anyString(), anyBoolean());
        verify(mockJudgmentRepository, never()).saveAll(anyList());
    }

    @ParameterizedTest
    @MethodSource("asOfServiceIdParams")
    void testJudgmentsTestMode(LocalDateTime asOf, String serviceId) {
        Judgment judgment = createJudgment(SERVICE_ID_1, asOf);

        List<Judgment> judgments = new ArrayList<>();
        judgments.add(judgment);

        setUpExpectedFindForRtlBehaviour(asOf, serviceId, judgments);

        scheduledReportService.generateReport(IS_TEST, asOf, serviceId);

        if (asOf == null) {
            assertNull(judgment.getReportedToRtl(), "Judgment reported to RTL date should be null");
        } else {
            assertEquals(asOf, judgment.getReportedToRtl(), "Judgment reported to RTL date should not be changed");
        }

        verifyExpectedFindForRtlBehaviour(asOf, serviceId);
        verify(mockJudgmentFileService)
            .createAndSendJudgmentFile(anyList(), any(LocalDateTime.class), eq(SERVICE_ID_1), eq(true));
        verify(mockJudgmentRepository, never()).saveAll(anyList());
    }

    private Judgment createJudgment(String serviceId, LocalDateTime reportedToRtl) {
        Judgment judgment = new Judgment();
        judgment.setServiceId(serviceId);
        judgment.setReportedToRtl(reportedToRtl);
        return judgment;
    }

    private Judgment createJudgment(long id, String serviceId) {
        Judgment judgment = createJudgment(serviceId, DATE_TIME_AS_OF);
        judgment.setId(id);
        return judgment;
    }

    private void setUpExpectedFindForRtlBehaviour(LocalDateTime asOf, String serviceId, List<Judgment> judgments) {
        if (asOf == null) {
            if (serviceId == null) {
                when(mockJudgmentRepository.findForRtl()).thenReturn(judgments);
            } else {
                when(mockJudgmentRepository.findForRtlServiceId(serviceId)).thenReturn(judgments);
            }
        } else {
            if (serviceId == null) {
                when(mockJudgmentRepository.findForRtlRerun(asOf)).thenReturn(judgments);
            } else {
                when(mockJudgmentRepository.findForRtlServiceIdRerun(asOf, serviceId)).thenReturn(judgments);
            }
        }
    }

    private void verifyExpectedFindForRtlBehaviour(LocalDateTime asOf, String serviceId) {
        if (asOf == null) {
            if (serviceId == null) {
                verify(mockJudgmentRepository).findForRtl();
            } else {
                verify(mockJudgmentRepository).findForRtlServiceId(serviceId);
            }
        } else {
            if (serviceId == null) {
                verify(mockJudgmentRepository).findForRtlRerun(asOf);
            } else {
                verify(mockJudgmentRepository).findForRtlServiceIdRerun(asOf, serviceId);
            }
        }
    }

    private static Stream<Arguments> asOfServiceIdParams() {
        return Stream.of(
          arguments(null, null),
          arguments(DATE_TIME_AS_OF, null),
          arguments(null, SERVICE_ID_1),
          arguments(DATE_TIME_AS_OF, SERVICE_ID_1)
        );
    }

    private static Stream<Arguments> asOfParams() {
        return Stream.of(
            arguments(DATE_TIME_AS_OF)
        );
    }
}
