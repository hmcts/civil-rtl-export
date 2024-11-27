package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.model.Defendant;
import uk.gov.hmcts.reform.civil.model.DefendantAddress;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.model.RegistrationType;
import uk.gov.hmcts.reform.civil.service.replace.CharacterReplacementConfigProperties;
import uk.gov.hmcts.reform.civil.service.replace.CharacterReplacementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    initializers = ConfigDataApplicationContextInitializer.class,
    classes = {JudgmentEventTransformerService.class, CharacterReplacementService.class}
)
@EnableConfigurationProperties(CharacterReplacementConfigProperties.class)
@ActiveProfiles("itest")
class JudgmentEventTransformerServiceIntTest {

    private static final String SERVICE_ID = "IT01";
    private static final String JUDGMENT_ID = "1001";
    private static final LocalDateTime JUDGMENT_EVENT_TIMESTAMP = LocalDateTime.of(2024, 11, 20, 12, 0, 0);
    private static final String COURT_CODE = "128";
    private static final String CCD_CASE_REF = "10000001";
    private static final String CASE_NUMBER = "0AA10001";
    private static final BigDecimal JUDGMENT_ADMIN_ORDER_TOTAL = new BigDecimal("23.10");
    private static final LocalDate JUDGMENT_ADMIN_ORDER_DATE = LocalDate.of(2024, 11, 1);
    private static final String DEFENDANT_1_PREFIX = "DEF1";
    private static final String DEFENDANT_2_PREFIX = "DEF2";

    private static final String JUDGMENT_ID_SUFFIX_1 = "-1";
    private static final String JUDGMENT_ID_SUFFIX_2 = "-2";

    private final JudgmentEventTransformerService judgmentEventTransformerService;

    @Autowired
    public JudgmentEventTransformerServiceIntTest(JudgmentEventTransformerService judgmentEventTransformerService) {
        this.judgmentEventTransformerService = judgmentEventTransformerService;
    }

    @Test
    void testTransformJudgmentEventOneDefendant() {
        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant();

        List<Judgment> judgments = judgmentEventTransformerService.transformJudgmentEvent(judgmentEvent, COURT_CODE);

        assertEquals(1, judgments.size(), "Unexpected number of judgments created");
        assertJudgment(judgments.get(0), JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1, DEFENDANT_1_PREFIX, "DD1 1DI");
    }

    @Test
    void testTransformJudgmentEventTwoDefendants() {
        JudgmentEvent judgmentEvent = createJudgmentEventTwoDefendants();

        List<Judgment> judgments = judgmentEventTransformerService.transformJudgmentEvent(judgmentEvent, COURT_CODE);

        assertEquals(2, judgments.size(), "Unexpected number of judgments created");
        assertJudgment(judgments.get(0), JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1, DEFENDANT_1_PREFIX, "DD1 1DI");
        assertJudgment(judgments.get(1), JUDGMENT_ID + JUDGMENT_ID_SUFFIX_2, DEFENDANT_2_PREFIX, "DD2 2DI");
    }

    private void assertJudgment(Judgment judgment,
                                String expectedJudgmentId,
                                String defendantPrefix,
                                String expectedPostcode) {
        assertEquals(SERVICE_ID, judgment.getServiceId(), "Unexpected ServiceId");
        assertEquals(expectedJudgmentId, judgment.getJudgmentId(), "Unexpected JudgmentId");
        assertEquals(JUDGMENT_EVENT_TIMESTAMP,
                     judgment.getJudgmentEventTimestamp(),
                     "Unexpected JudgmentEventTimestamp");
        assertEquals(COURT_CODE, judgment.getCourtCode(), "Unexpected CourtCode");
        assertEquals(CCD_CASE_REF, judgment.getCcdCaseRef(), "Unexpected CcdCaseRef");
        assertEquals(CASE_NUMBER, judgment.getCaseNumber(), "Unexpected CaseNumber");
        assertEquals(JUDGMENT_ADMIN_ORDER_TOTAL,
                     judgment.getJudgmentAdminOrderTotal(),
                     "Unexpected JudgmentAdminOrderTotal");
        assertEquals(JUDGMENT_ADMIN_ORDER_DATE,
                     judgment.getJudgmentAdminOrderDate(),
                     "Unexpected JudgmentAdminOrderDate");
        assertEquals(RegistrationType.JUDGMENT_REGISTERED.getRegType(),
                     judgment.getRegistrationType(),
                     "Unexpected RegistrationType");
        assertNull(judgment.getCancellationDate(), "CancellationDate should be null");

        String expectedDefendantName = defendantPrefix + "FIRSTNAME " + defendantPrefix + "LASTNAME";
        assertEquals(expectedDefendantName, judgment.getDefendantName(), "Unexpected DefendantName");

        String addressLinePrefix = defendantPrefix + " ADDRESS LINE ";
        assertEquals(addressLinePrefix + 1, judgment.getDefendantAddressLine1(), "Unexpected DefendantAddressLine1");
        assertEquals(addressLinePrefix + 2, judgment.getDefendantAddressLine2(), "Unexpected DefendantAddressLine2");
        assertEquals(addressLinePrefix + 3, judgment.getDefendantAddressLine3(), "Unexpected DefendantAddressLine3");
        assertEquals(addressLinePrefix + 4, judgment.getDefendantAddressLine4(), "Unexpected DefendantAddressLine4");
        assertEquals(addressLinePrefix + 5, judgment.getDefendantAddressLine5(), "Unexpected DefendantAddressLine5");
        assertEquals(expectedPostcode, judgment.getDefendantAddressPostcode(), "Unexpected DefendantAddressPostcode");

        assertNull(judgment.getDefendantDob(), "DefendantDob should be null");
        assertNull(judgment.getReportedToRtl(), "ReportedToRtl should be null");
    }

    private JudgmentEvent createJudgmentEventOneDefendant() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();

        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setJudgmentId(JUDGMENT_ID);
        judgmentEvent.setJudgmentEventTimeStamp(JUDGMENT_EVENT_TIMESTAMP);
        judgmentEvent.setCourtEpimsId("123456");
        judgmentEvent.setCcdCaseRef(CCD_CASE_REF);
        judgmentEvent.setCaseNumber(CASE_NUMBER);
        judgmentEvent.setJudgmentAdminOrderTotal(JUDGMENT_ADMIN_ORDER_TOTAL);
        judgmentEvent.setJudgmentAdminOrderDate(JUDGMENT_ADMIN_ORDER_DATE);
        judgmentEvent.setRegistrationType(RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setDefendant1(createDefendant(DEFENDANT_1_PREFIX, "DD1 1DÏ"));

        return judgmentEvent;
    }

    private JudgmentEvent createJudgmentEventTwoDefendants() {
        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant();
        judgmentEvent.setDefendant2(createDefendant(DEFENDANT_2_PREFIX, "DD2 2DÏ"));

        return judgmentEvent;
    }

    private Defendant createDefendant(String prefix, String postcode) {
        DefendantAddress defendantAddress = new DefendantAddress();
        defendantAddress.setDefendantAddressLine1(prefix + " ADDRESS LÏNE 1");
        defendantAddress.setDefendantAddressLine2(prefix + " ADDRESS LÏNE 2");
        defendantAddress.setDefendantAddressLine3(prefix + " ADDRESS LÏNE 3");
        defendantAddress.setDefendantAddressLine4(prefix + " ADDRESS LÏNE 4");
        defendantAddress.setDefendantAddressLine5(prefix + " ADDRESS LÏNE 5");
        defendantAddress.setDefendantPostcode(postcode);

        Defendant defendant = new Defendant();
        defendant.setDefendantName(prefix + "FÏRSTNAME " + prefix + "LASTNAME");
        defendant.setDefendantAddress(defendantAddress);

        return defendant;
    }
}
