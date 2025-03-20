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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest(includeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = ScheduledTaskRunner.class)
)
@ActiveProfiles(profiles = "itest")
@Sql(scripts = {"judgment_repository_int_test.sql"})
class JudgmentRepositoryIntTest {

    private static final String JUD_1_SERVICE_ID = "IT01";
    private static final String JUD_1_JUDGMENT_ID = "1001";
    private static final LocalDateTime JUD_1_JUDGMENT_EVENT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 1, 0, 0);
    private static final String JUD_1_CASE_NUMBER = "0AA10001";
    private static final String JUD_1_DEF1_PREFIX = "Jud1Def1";
    private static final String JUD_1_DEF1_POSTCODE = "JD1 1DD";

    private static final String JUD_2_SERVICE_ID = "IT02";
    private static final String JUD_2_JUDGMENT_ID = "2002";
    private static final LocalDateTime JUD_2_JUDGMENT_EVENT_TIMESTAMP = LocalDateTime.of(2024, 2, 2, 2, 0, 0);
    private static final String JUD_2_CASE_NUMBER = "0AA20002";
    private static final String JUD_2_DEF1_PREFIX = "Jud2Def1";
    private static final String JUD_2_DEF1_POSTCODE = "JD2 1DD";
    private static final String JUD_2_DEF2_PREFIX = "Jud2Def2";
    private static final String JUD_2_DEF2_POSTCODE = "JD2 2DD";

    private static final String JUDGMENT_ID_SUFFIX_1 = "-1";
    private static final String JUDGMENT_ID_SUFFIX_2 = "-2";

    private final JudgmentRepository judgmentRepository;

    @Autowired
    public JudgmentRepositoryIntTest(JudgmentRepository judgmentRepository) {
        this.judgmentRepository = judgmentRepository;
    }

    @Test
    void testFindByEventDetailsSelectCriteria() {
        List<Judgment> judgments =
            findByEventDetails(JUD_1_SERVICE_ID, JUD_1_JUDGMENT_ID, JUD_1_JUDGMENT_EVENT_TIMESTAMP, JUD_1_CASE_NUMBER);

        assertNotNull(judgments, "Returned judgments should not be null");
        assertEquals(1, judgments.size(), "Unexpected number of judgments returned");

        assertJudgment(judgments.getFirst(),
                       JUD_1_SERVICE_ID,
                       JUD_1_JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                       JUD_1_JUDGMENT_EVENT_TIMESTAMP,
                       JUD_1_CASE_NUMBER,
                       JUD_1_DEF1_PREFIX,
                       JUD_1_DEF1_POSTCODE);
    }

    @Test
    void testFindByEventDetailsTwoDefendants() {
        List<Judgment> judgments =
            findByEventDetails(JUD_2_SERVICE_ID, JUD_2_JUDGMENT_ID, JUD_2_JUDGMENT_EVENT_TIMESTAMP, JUD_2_CASE_NUMBER);

        assertNotNull(judgments, "Returned judgments should not be null");
        assertEquals(2, judgments.size(), "Unexpected number of judgments returned");

        assertJudgment(judgments.get(0),
                       JUD_2_SERVICE_ID,
                       JUD_2_JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                       JUD_2_JUDGMENT_EVENT_TIMESTAMP,
                       JUD_2_CASE_NUMBER,
                       JUD_2_DEF1_PREFIX,
                       JUD_2_DEF1_POSTCODE);
        assertJudgment(judgments.get(1),
                       JUD_2_SERVICE_ID,
                       JUD_2_JUDGMENT_ID + JUDGMENT_ID_SUFFIX_2,
                       JUD_2_JUDGMENT_EVENT_TIMESTAMP,
                       JUD_2_CASE_NUMBER,
                       JUD_2_DEF2_PREFIX,
                       JUD_2_DEF2_POSTCODE);
    }

    private List<Judgment> findByEventDetails(String serviceId,
                                              String judgmentId,
                                              LocalDateTime judgmentEventTimestamp,
                                              String caseNumber) {

        return judgmentRepository.findByEventDetails(serviceId, judgmentId, judgmentEventTimestamp, caseNumber);
    }

    private void assertJudgment(Judgment judgment,
                                String expectedServiceId,
                                String expectedJudgmentId,
                                LocalDateTime expectedJudgmentEventTimestamp,
                                String expectedCaseNumber,
                                String defendantPrefix,
                                String expectedDefendantPostcode) {

        assertEquals(expectedServiceId, judgment.getServiceId(), "Unexpected serviceId");
        assertEquals(expectedJudgmentId, judgment.getJudgmentId(), "Unexpected judgmentId");
        assertEquals(expectedJudgmentEventTimestamp,
                     judgment.getJudgmentEventTimestamp(),
                     "Unexpected judgmentEventTimestamp");
        assertEquals(expectedCaseNumber, judgment.getCaseNumber(), "Unexpected caseNumber");

        String expectedDefendantName = defendantPrefix + "FirstName " + defendantPrefix + "LastName";
        assertEquals(expectedDefendantName, judgment.getDefendantName(), "Unexpected defendantName");
        assertEquals(defendantPrefix + " Address Line 1",
                     judgment.getDefendantAddressLine1(),
                     "Unexpected defendantAddressLine1");
        assertEquals(expectedDefendantPostcode,
                     judgment.getDefendantAddressPostcode(),
                     "Unexpected defendantAddressPostcode");
    }
}
