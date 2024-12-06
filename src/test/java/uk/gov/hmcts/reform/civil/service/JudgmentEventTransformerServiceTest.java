package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.model.Defendant;
import uk.gov.hmcts.reform.civil.model.DefendantAddress;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.model.RegistrationType;
import uk.gov.hmcts.reform.civil.service.replace.CharacterReplacementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudgmentEventTransformerServiceTest {

    private static final String SERVICE_ID = "UT01";
    private static final String JUDGMENT_ID = "1001";
    private static final LocalDateTime JUDGMENT_EVENT_TIMESTAMP = LocalDateTime.of(2024, 11, 19, 1, 0, 0);
    private static final String COURT_EPIMS_ID = "123456";
    private static final String COURT_CODE = "100";
    private static final String CCD_CASE_REF = "10000001";
    private static final String CASE_NUMBER = "0AA10001";
    private static final BigDecimal JUDGMENT_ADMIN_ORDER_TOTAL = new BigDecimal("50.00");
    private static final LocalDate JUDGMENT_ADMIN_ORDER_DATE = LocalDate.of(2024, 11, 18);
    private static final LocalDate CANCELLATION_DATE = LocalDate.of(2024, 11, 17);
    private static final String DEFENDANT_1_PREFIX = "Def1";
    private static final String DEFENDANT_2_PREFIX = "Def2";
    private static final String DEFENDANT_1_POSTCODE = "DD1 1DD";
    private static final String DEFENDANT_2_POSTCODE = "DD2 2DD";
    private static final LocalDate DEFENDANT_1_DOB = LocalDate.of(2001, 1, 1);
    private static final LocalDate DEFENDANT_2_DOB = LocalDate.of(2002, 2, 2);

    private static final String JUDGMENT_ID_SUFFIX_1 = "-1";
    private static final String JUDGMENT_ID_SUFFIX_2 = "-2";

    private static final int MAX_LENGTH_DEFENDANT_NAME = 70;
    private static final int MAX_LENGTH_DEFENDANT_ADDRESS_LINE = 35;
    private static final int MAX_LENGTH_DEFENDANT_POSTCODE = 8;

    @Mock
    private CharacterReplacementService mockCharacterReplacementService;

    private JudgmentEventTransformerService judgmentEventTransformerService;

    @BeforeEach
    void setUp() {
        judgmentEventTransformerService = new JudgmentEventTransformerService(mockCharacterReplacementService);
    }

    @Test
    void testTransformJudgmentEventMandatoryFieldsOnly() {
        configureDefendantNameMockBehaviour(DEFENDANT_1_PREFIX);
        configureDefendantAddressLineMockBehaviour(DEFENDANT_1_PREFIX, 1);
        configureDefendantAddressLineMockBehaviour(null);
        configureDefendantPostcodeMockBehaviour(DEFENDANT_1_POSTCODE);

        List<Judgment> judgments = judgmentEventTransformerService
            .transformJudgmentEvent(createJudgmentEventMandatoryFieldsOnly(), COURT_CODE);
        assertNumberOfJudgments(1, judgments);

        Judgment judgment = judgments.get(0);
        assertEquals(SERVICE_ID, judgment.getServiceId(), "Unexpected ServiceId");
        assertEquals(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1, judgment.getJudgmentId(), "Unexpected JudgmentId");
        assertEquals(JUDGMENT_EVENT_TIMESTAMP,
                     judgment.getJudgmentEventTimestamp(),
                     "Unexpected JudgmentEventTimestamp");
        assertEquals(COURT_CODE, judgment.getCourtCode(), "Unexpected CourtCode");
        assertEquals(CCD_CASE_REF, judgment.getCcdCaseRef(), "Unexpected CCDCaseRef");
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
        assertEquals(createDefendantName(DEFENDANT_1_PREFIX), judgment.getDefendantName(), "Unexpected DefendantName");
        assertDefendantAddressLine(DEFENDANT_1_PREFIX, 1, judgment.getDefendantAddressLine1());
        assertNull(judgment.getDefendantAddressLine2(), "DefendantAddressLine2 should be null");
        assertNull(judgment.getDefendantAddressLine3(), "DefendantAddressLine3 should be null");
        assertNull(judgment.getDefendantAddressLine4(), "DefendantAddressLine4 should be null");
        assertNull(judgment.getDefendantAddressLine5(), "DefendantAddressLine5 should be null");
        assertEquals(DEFENDANT_1_POSTCODE,
                     judgment.getDefendantAddressPostcode(),
                     "Unexpected DefendantAddressPostcode");
        assertNull(judgment.getDefendantDob(), "DefendantDob should be null");
        assertNull(judgment.getReportedToRtl(), "ReportedToRtl should be null");

        verifyDefendantNameMockBehaviour(DEFENDANT_1_PREFIX);
        verifyDefendantAddressLineMockBehaviour(DEFENDANT_1_PREFIX, 1);
        verify(mockCharacterReplacementService, times(4)).replaceCharacters(null, MAX_LENGTH_DEFENDANT_ADDRESS_LINE);
        verifyDefendantPostcodeMockBehaviour(DEFENDANT_1_POSTCODE);
    }

    @Test
    void testTransformJudgmentEventOneDefendant() {
        configureDefendantMockBehaviour(DEFENDANT_1_PREFIX, DEFENDANT_1_POSTCODE);

        List<Judgment> judgments =
            judgmentEventTransformerService.transformJudgmentEvent(createJudgmentEventOneDefendant(), COURT_CODE);

        assertNumberOfJudgments(1, judgments);
        assertJudgment(judgments.get(0),
                       JUDGMENT_ID_SUFFIX_1,
                       DEFENDANT_1_PREFIX,
                       DEFENDANT_1_POSTCODE,
                       DEFENDANT_1_DOB);

        verifyDefendantMockBehaviour(DEFENDANT_1_PREFIX, DEFENDANT_1_POSTCODE);
    }

    @Test
    void testTransformJudgmentEventTwoDefendants() {
        configureDefendantMockBehaviour(DEFENDANT_1_PREFIX, DEFENDANT_1_POSTCODE);
        configureDefendantMockBehaviour(DEFENDANT_2_PREFIX, DEFENDANT_2_POSTCODE);

        List<Judgment> judgments =
            judgmentEventTransformerService.transformJudgmentEvent(createJudgmentEventTwoDefendants(), COURT_CODE);

        assertNumberOfJudgments(2, judgments);
        assertJudgment(judgments.get(0),
                       JUDGMENT_ID_SUFFIX_1,
                       DEFENDANT_1_PREFIX,
                       DEFENDANT_1_POSTCODE,
                       DEFENDANT_1_DOB);
        assertJudgment(judgments.get(1),
                       JUDGMENT_ID_SUFFIX_2,
                       DEFENDANT_2_PREFIX,
                       DEFENDANT_2_POSTCODE,
                       DEFENDANT_2_DOB);

        verifyDefendantMockBehaviour(DEFENDANT_1_PREFIX, DEFENDANT_1_POSTCODE);
        verifyDefendantMockBehaviour(DEFENDANT_2_PREFIX, DEFENDANT_2_POSTCODE);
    }

    private void configureDefendantMockBehaviour(String prefix, String postcode) {
        configureDefendantNameMockBehaviour(prefix);

        configureDefendantAddressLineMockBehaviour(prefix, 1);
        configureDefendantAddressLineMockBehaviour(prefix, 2);
        configureDefendantAddressLineMockBehaviour(prefix, 3);
        configureDefendantAddressLineMockBehaviour(prefix, 4);
        configureDefendantAddressLineMockBehaviour(prefix, 5);
        configureDefendantPostcodeMockBehaviour(postcode);
    }

    private void configureDefendantNameMockBehaviour(String prefix) {
        String defendantName = createDefendantName(prefix);
        when(mockCharacterReplacementService.replaceCharacters(defendantName, MAX_LENGTH_DEFENDANT_NAME))
            .thenReturn(defendantName);
    }

    private void configureDefendantAddressLineMockBehaviour(String prefix, int lineNumber) {
        String addressLine = createDefendantAddressLine(prefix, lineNumber);
        configureDefendantAddressLineMockBehaviour(addressLine);
    }

    private void configureDefendantAddressLineMockBehaviour(String addressLine) {
        when(mockCharacterReplacementService.replaceCharacters(addressLine, MAX_LENGTH_DEFENDANT_ADDRESS_LINE))
            .thenReturn(addressLine);
    }

    private void configureDefendantPostcodeMockBehaviour(String postcode) {
        when(mockCharacterReplacementService.replaceCharacters(postcode, MAX_LENGTH_DEFENDANT_POSTCODE))
            .thenReturn(postcode);
    }

    private void verifyDefendantMockBehaviour(String prefix, String postcode) {
        verifyDefendantNameMockBehaviour(prefix);

        verifyDefendantAddressLineMockBehaviour(prefix, 1);
        verifyDefendantAddressLineMockBehaviour(prefix, 2);
        verifyDefendantAddressLineMockBehaviour(prefix, 3);
        verifyDefendantAddressLineMockBehaviour(prefix, 4);
        verifyDefendantAddressLineMockBehaviour(prefix, 5);
        verifyDefendantPostcodeMockBehaviour(postcode);
    }

    private void verifyDefendantNameMockBehaviour(String prefix) {
        verify(mockCharacterReplacementService)
            .replaceCharacters(createDefendantName(prefix), MAX_LENGTH_DEFENDANT_NAME);
    }

    private void verifyDefendantAddressLineMockBehaviour(String prefix, int lineNumber) {
        String addressLine = createDefendantAddressLine(prefix, lineNumber);
        verify(mockCharacterReplacementService).replaceCharacters(addressLine, MAX_LENGTH_DEFENDANT_ADDRESS_LINE);
    }

    private void verifyDefendantPostcodeMockBehaviour(String postcode) {
        verify(mockCharacterReplacementService).replaceCharacters(postcode, MAX_LENGTH_DEFENDANT_POSTCODE);
    }

    private void assertNumberOfJudgments(int expectedNumJudgments, List<Judgment> judgments) {
        assertNotNull(judgments, "List of judgments returned by transformer should not be null");
        assertEquals(expectedNumJudgments, judgments.size(), "Unexpected number of judgments returned by transformer");
    }

    private void assertJudgment(Judgment judgment,
                                String judgmentIdSuffix,
                                String defendantPrefix,
                                String defendantPostcode,
                                LocalDate defendantDob) {
        assertEquals(SERVICE_ID, judgment.getServiceId(), "Unexpected ServiceId");
        assertEquals(JUDGMENT_ID + judgmentIdSuffix, judgment.getJudgmentId(), "Unexpected JudgmentId");
        assertEquals(JUDGMENT_EVENT_TIMESTAMP,
                     judgment.getJudgmentEventTimestamp(),
                     "Unexpected JudgmentEventTimestamp");
        assertEquals(COURT_CODE, judgment.getCourtCode(), "Unexpected CourtCode");
        assertEquals(CCD_CASE_REF, judgment.getCcdCaseRef(), "Unexpected CCDCaseRef");
        assertEquals(CASE_NUMBER, judgment.getCaseNumber(), "Unexpected CaseNumber");
        assertEquals(JUDGMENT_ADMIN_ORDER_TOTAL,
                     judgment.getJudgmentAdminOrderTotal(),
                     "Unexpected JudgmentAdminOrderTotal");
        assertEquals(JUDGMENT_ADMIN_ORDER_DATE,
                     judgment.getJudgmentAdminOrderDate(),
                     "Unexpected JudgmentAdminOrderDate");
        assertEquals(RegistrationType.ADMIN_ORDER_REVOKED.getRegType(),
                     judgment.getRegistrationType(),
                     "Unexpected RegistrationType");
        assertEquals(CANCELLATION_DATE, judgment.getCancellationDate(), "Unexpected CancellationDate");

        assertDefendant(judgment, defendantPrefix, defendantPostcode, defendantDob);

        assertNull(judgment.getReportedToRtl(), "ReportedToRtl should be null");
    }

    private void assertDefendant(Judgment judgment, String prefix, String postcode, LocalDate dob) {
        assertEquals(createDefendantName(prefix), judgment.getDefendantName(), "Unexpected DefendantName");

        assertDefendantAddressLine(prefix, 1, judgment.getDefendantAddressLine1());
        assertDefendantAddressLine(prefix, 2, judgment.getDefendantAddressLine2());
        assertDefendantAddressLine(prefix, 3, judgment.getDefendantAddressLine3());
        assertDefendantAddressLine(prefix, 4, judgment.getDefendantAddressLine4());
        assertDefendantAddressLine(prefix, 5, judgment.getDefendantAddressLine5());
        assertEquals(postcode, judgment.getDefendantAddressPostcode(), "Unexpected DefendantAddressPostcode");

        assertEquals(dob, judgment.getDefendantDob(), "Unexpected DefendantDob");
    }

    private void assertDefendantAddressLine(String prefix, int lineNumber, String addressLine) {
        assertEquals(createDefendantAddressLine(prefix, lineNumber),
                     addressLine,
                     "Unexpected DefendantAddressLine" + lineNumber);
    }

    private JudgmentEvent createJudgmentEventMandatoryFieldsOnly() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();

        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setJudgmentId(JUDGMENT_ID);
        judgmentEvent.setJudgmentEventTimeStamp(JUDGMENT_EVENT_TIMESTAMP);
        judgmentEvent.setCourtEpimsId(COURT_EPIMS_ID);
        judgmentEvent.setCcdCaseRef(CCD_CASE_REF);
        judgmentEvent.setCaseNumber(CASE_NUMBER);
        judgmentEvent.setJudgmentAdminOrderTotal(JUDGMENT_ADMIN_ORDER_TOTAL);
        judgmentEvent.setJudgmentAdminOrderDate(JUDGMENT_ADMIN_ORDER_DATE);
        judgmentEvent.setRegistrationType(RegistrationType.JUDGMENT_REGISTERED);

        DefendantAddress address = new DefendantAddress();
        address.setDefendantAddressLine1(createDefendantAddressLine(DEFENDANT_1_PREFIX, 1));
        address.setDefendantPostcode(DEFENDANT_1_POSTCODE);

        Defendant defendant = new Defendant();
        defendant.setDefendantName(createDefendantName(DEFENDANT_1_PREFIX));
        defendant.setDefendantAddress(address);

        judgmentEvent.setDefendant1(defendant);

        return judgmentEvent;
    }

    private JudgmentEvent createJudgmentEventOneDefendant() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();

        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setJudgmentId(JUDGMENT_ID);
        judgmentEvent.setJudgmentEventTimeStamp(JUDGMENT_EVENT_TIMESTAMP);
        judgmentEvent.setCourtEpimsId(COURT_EPIMS_ID);
        judgmentEvent.setCcdCaseRef(CCD_CASE_REF);
        judgmentEvent.setCaseNumber(CASE_NUMBER);
        judgmentEvent.setJudgmentAdminOrderTotal(JUDGMENT_ADMIN_ORDER_TOTAL);
        judgmentEvent.setJudgmentAdminOrderDate(JUDGMENT_ADMIN_ORDER_DATE);
        judgmentEvent.setRegistrationType(RegistrationType.ADMIN_ORDER_REVOKED);
        judgmentEvent.setCancellationDate(CANCELLATION_DATE);
        judgmentEvent.setDefendant1(createDefendant(DEFENDANT_1_PREFIX, DEFENDANT_1_POSTCODE, DEFENDANT_1_DOB));

        return judgmentEvent;
    }

    private JudgmentEvent createJudgmentEventTwoDefendants() {
        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant();
        judgmentEvent.setDefendant2(createDefendant(DEFENDANT_2_PREFIX, DEFENDANT_2_POSTCODE, DEFENDANT_2_DOB));

        return judgmentEvent;
    }

    private Defendant createDefendant(String prefix, String postcode, LocalDate dob) {
        Defendant defendant = new Defendant();

        defendant.setDefendantName(createDefendantName(prefix));

        DefendantAddress address = new DefendantAddress();
        address.setDefendantAddressLine1(createDefendantAddressLine(prefix, 1));
        address.setDefendantAddressLine2(createDefendantAddressLine(prefix, 2));
        address.setDefendantAddressLine3(createDefendantAddressLine(prefix, 3));
        address.setDefendantAddressLine4(createDefendantAddressLine(prefix, 4));
        address.setDefendantAddressLine5(createDefendantAddressLine(prefix, 5));
        address.setDefendantPostcode(postcode);

        defendant.setDefendantAddress(address);
        defendant.setDefendantDateOfBirth(dob);

        return defendant;
    }

    private String createDefendantName(String prefix) {
        return prefix + "FirstName " + prefix + "LastName";
    }

    private String createDefendantAddressLine(String prefix, int lineNumber) {
        return prefix + " Address Line " + lineNumber;
    }
}
