package uk.gov.hmcts.reform.civil.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.service.task.ScheduledTaskRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = ScheduledTaskRunner.class)
)
@ActiveProfiles("itest")
@Sql(scripts = {"judgment_repository_report_int_test.sql"})
class JudgmentRepositoryReportIntTest {

    private static final String SERVICE_ID_1 = "Sid1";
    private static final String SERVICE_ID_2 = "Sid2";

    private static final String JUDGMENT_ID_1 = "JUDG-1111-1111";
    private static final String JUDGMENT_ID_2 = "JUDG-2222-2222";
    private static final String JUDGMENT_ID_3 = "JUDG-3333-3333";
    private static final String JUDGMENT_ID_4 = "JUDG-4444-4444";

    private final JudgmentRepository judgmentRepository;

    @Autowired
    public JudgmentRepositoryReportIntTest(JudgmentRepository judgmentRepository) {
        this.judgmentRepository = judgmentRepository;
    }

    @Test
    void testFindForRtl() {
        List<Judgment> expectedJudgments = new ArrayList<>();
        expectedJudgments.add(createExpectedJudgment(SERVICE_ID_1, JUDGMENT_ID_1));
        expectedJudgments.add(createExpectedJudgment(SERVICE_ID_2, JUDGMENT_ID_3));

        List<Judgment> results = judgmentRepository.findForRtl();
        assertJudgments(results, expectedJudgments);
    }

    @Test
    @Sql(scripts = {"judgment_repository_report_int_test_all_reported_to_rtl.sql"})
    void testFindForRtlNoJudgments() {
        List<Judgment> results = judgmentRepository.findForRtl();
        assertJudgments(results, Collections.emptyList());
    }

    @Test
    void testFindForRtlRerun() {
        LocalDateTime asOf = LocalDate.now().minusDays(1).atStartOfDay();

        List<Judgment> expectedJudgments = new ArrayList<>();
        expectedJudgments.add(createExpectedJudgment(SERVICE_ID_1, JUDGMENT_ID_2, asOf));
        expectedJudgments.add(createExpectedJudgment(SERVICE_ID_2, JUDGMENT_ID_4, asOf));

        List<Judgment> results = judgmentRepository.findForRtlRerun(asOf);
        assertJudgments(results, expectedJudgments);
    }

    @Test
    void testFindForRtlRerunNoJudgments() {
        LocalDateTime asOf = LocalDate.now().minusDays(2).atStartOfDay();

        List<Judgment> results = judgmentRepository.findForRtlRerun(asOf);
        assertJudgments(results, Collections.emptyList());
    }

    @Test
    void testFindForRtlServiceId() {
        List<Judgment> expectedJudgments = new ArrayList<>();
        expectedJudgments.add(createExpectedJudgment(SERVICE_ID_1, JUDGMENT_ID_1));

        List<Judgment> results = judgmentRepository.findForRtlServiceId(SERVICE_ID_1);
        assertJudgments(results, expectedJudgments);
    }

    @Test
    @Sql(scripts = {"judgment_repository_report_int_test_all_reported_to_rtl.sql"})
    void testFindForRtlServiceIdNoJudgments() {
        List<Judgment> results = judgmentRepository.findForRtlServiceId(SERVICE_ID_1);
        assertJudgments(results, Collections.emptyList());
    }

    @Test
    void testFindForRtlServiceIdRerun() {
        LocalDateTime asOf = LocalDate.now().minusDays(1).atStartOfDay();

        List<Judgment> expectedJudgments = new ArrayList<>();
        expectedJudgments.add(createExpectedJudgment(SERVICE_ID_2, JUDGMENT_ID_4, asOf));

        List<Judgment> results = judgmentRepository.findForRtlServiceIdRerun(asOf, SERVICE_ID_2);
        assertJudgments(results, expectedJudgments);
    }

    @Test
    void testFindForRtlServiceIdRerunNoJudgments() {
        LocalDateTime asOf = LocalDate.now().minusDays(2).atStartOfDay();

        List<Judgment> results = judgmentRepository.findForRtlServiceIdRerun(asOf, SERVICE_ID_2);
        assertJudgments(results, Collections.emptyList());
    }

    private Judgment createExpectedJudgment(String serviceId, String judgmentId, LocalDateTime reportedToRtl) {
        Judgment judgment = createExpectedJudgment(serviceId, judgmentId);
        judgment.setReportedToRtl(reportedToRtl);
        return judgment;
    }

    private Judgment createExpectedJudgment(String serviceId, String judgmentId) {
        Judgment judgment = new Judgment();

        judgment.setServiceId(serviceId);
        judgment.setJudgmentId(judgmentId);

        return judgment;
    }

    private void assertJudgments(List<Judgment> results, List<Judgment> expectedJudgments) {
        assertNotNull(results, "List of judgments returned should not be null");
        assertEquals(results.size(), expectedJudgments.size(), "Unexpected number of judgments returned");

        for (int index = 0; index < expectedJudgments.size(); index++) {
            assertJudgment(results.get(index), expectedJudgments.get(index));
        }
    }

    private void assertJudgment(Judgment judgment, Judgment expectedJudgment) {
        assertEquals(expectedJudgment.getServiceId(), judgment.getServiceId(), "Judgment has unexpected service id");
        assertEquals(expectedJudgment.getJudgmentId(), judgment.getJudgmentId(), "Judgment has unexpected judgment id");

        LocalDateTime expectedReportedToRtl = expectedJudgment.getReportedToRtl();
        if (expectedReportedToRtl == null) {
            assertNull(judgment.getReportedToRtl(), "Judgment reported to RTL date should be null");
        } else {
            assertEquals(expectedReportedToRtl,
                         judgment.getReportedToRtl(),
                         "Judgment has unexpected reported to RTL value");
        }
    }
}
