package uk.gov.hmcts.reform.civil.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.civil.domain.Judgment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DataJpaTest
@ActiveProfiles("itest")
@Sql(scripts = {"test.sql"})
class JudgmentRepositoryIntTest {

    private static final String SERVICE_ID_1 = "Sid1";

    private static final boolean IS_RERUN = true;
    private static final boolean NOT_RERUN = false;

    private static final String JUDGMENT_ID_1 = "JUDG-1111-1111";
    private static final String JUDGMENT_ID_2 = "JUDG-2222-2222";
    private static final String JUDGMENT_ID_3 = "JUDG-3333-3333";
    private static final String JUDGMENT_ID_4 = "JUDG-4444-4444";

    private final JudgmentRepository judgmentRepository;

    @Autowired
    public JudgmentRepositoryIntTest(JudgmentRepository judgmentRepository) {
        this.judgmentRepository = judgmentRepository;
    }

    @ParameterizedTest
    @MethodSource("findForUpdateParams")
    void testFindForUpdate(boolean rerun, LocalDateTime asOf, List<String> expectedJudgmentIds) {
        List<Judgment> results = judgmentRepository.findForUpdate(rerun, asOf);
        assertJudgments(results, expectedJudgmentIds);
    }

    @Test
    void testFindForUpdateNoUnsentJudgments() {
        // Change test data so there are no judgments with a null reported to RTL date
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Judgment> judgments = judgmentRepository.findAllById(List.of(1L, 3L));
        judgments.forEach(judgment -> judgment.setReportedToRtl(currentDateTime));
        judgmentRepository.saveAll(judgments);

        List<Judgment> results = judgmentRepository.findForUpdate(NOT_RERUN, null);
        assertJudgments(results, Collections.emptyList());
    }

    @ParameterizedTest
    @MethodSource("findForUpdateByServiceIdParams")
    void testFindForUpdateByServiceId(boolean rerun, LocalDateTime asOf, List<String> expectedJudgmentIds) {
        List<Judgment> results = judgmentRepository.findForUpdateByServiceId(rerun, asOf, SERVICE_ID_1);
        assertJudgments(results, expectedJudgmentIds);
    }

    @Test
    void testFindForUpdateByServiceIdNoUnsentJudgments() {
        // Change test data so that there are no judgments for SERVICE_ID_1 with a null reported to RTL date
        LocalDateTime currentDateTime = LocalDateTime.now();
        List<Judgment> judgments = judgmentRepository.findAllById(List.of(1L));
        judgments.forEach(judgment -> judgment.setReportedToRtl(currentDateTime));
        judgmentRepository.saveAll(judgments);

        List<Judgment> results = judgmentRepository.findForUpdateByServiceId(NOT_RERUN, null, SERVICE_ID_1);
        assertJudgments(results, Collections.emptyList());
    }

    private void assertJudgments(List<Judgment> judgments, List<String> expectedJudgmentIds) {
        assertNotNull(judgments, "List of judgments should not be null");
        assertEquals(expectedJudgmentIds.size(), judgments.size(), "Unexpected number of judgments returned");

        int index = 0;
        for (Judgment judgment : judgments) {
            assertEquals(expectedJudgmentIds.get(index), judgment.getJudgmentId(), "Unexpected judgment id");
            index++;
        }
    }

    private static Stream<Arguments> findForUpdateParams() {
        LocalDateTime oneDayAgo = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime twoDaysAgo = LocalDate.now().minusDays(2).atStartOfDay();

        return Stream.of(
            arguments(NOT_RERUN, null, List.of(JUDGMENT_ID_1, JUDGMENT_ID_3)),
            // The asOf date is ignored if rerun is false, so results will be identical to when asOf is null
            arguments(NOT_RERUN, oneDayAgo, List.of(JUDGMENT_ID_1, JUDGMENT_ID_3)),
            // There should always be an asOf date if rerun is true, so this combination should never occur
            arguments(IS_RERUN, null, Collections.emptyList()),
            arguments(IS_RERUN, oneDayAgo, List.of(JUDGMENT_ID_2, JUDGMENT_ID_4)),
            arguments(IS_RERUN, twoDaysAgo, Collections.emptyList())
        );
    }

    private static Stream<Arguments> findForUpdateByServiceIdParams() {
        LocalDateTime oneDayAgo = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime twoDaysAgo = LocalDate.now().minusDays(2).atStartOfDay();

        return Stream.of(
            arguments(NOT_RERUN, null, List.of(JUDGMENT_ID_1)),
            // The asOf date is ignored if rerun is false, so results will be identical to when asOf is null
            arguments(NOT_RERUN, oneDayAgo, List.of(JUDGMENT_ID_1)),
            // There should always be an asOf date if rerun is true, so this combination should never occur
            arguments(IS_RERUN, null, Collections.emptyList()),
            arguments(IS_RERUN, oneDayAgo, List.of(JUDGMENT_ID_2)),
            arguments(IS_RERUN, twoDaysAgo, Collections.emptyList())
        );
    }

    @Test
    void testDeleteJudgmentsBefore() {
        assertTrue(judgmentRepository.existsById(5L),
                   "Judgment with reported to RTL date inside threshold should exist");
        assertTrue(judgmentRepository.existsById(6L),
                   "Judgment with reported to RTL date on threshold should exist");
        assertTrue(judgmentRepository.existsById(7L),
                   "Judgment with reported to RTL date outside threshold should exist");

        LocalDateTime dateOfDeletion = LocalDate.now().minusDays(90).atStartOfDay();
        int deletedRowCount = judgmentRepository.deleteJudgmentsBefore(dateOfDeletion);

        assertEquals(1, deletedRowCount, "Unexpected number of rows deleted");
        assertTrue(judgmentRepository.existsById(5L),
                   "Judgment with reported to RTL date inside threshold should not have been deleted");
        assertTrue(judgmentRepository.existsById(6L),
                   "Judgment with reported to RTL date on threshold should not have been deleted");
        assertFalse(judgmentRepository.existsById(7L),
                    "Judgment with reported to RTL date outside threshold should have been deleted");
    }
}
