package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.WireMockIntTestBase;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.exception.DifferentNumberOfDefendantsException;
import uk.gov.hmcts.reform.civil.exception.MissingCancellationDateException;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedEpimsIdException;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedServiceIdException;
import uk.gov.hmcts.reform.civil.exception.UpdateExistingJudgmentException;
import uk.gov.hmcts.reform.civil.model.Defendant;
import uk.gov.hmcts.reform.civil.model.DefendantAddress;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.model.RegistrationType;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("itest")
@Sql(scripts = {"judgment_event_service_int_test.sql"})
@Transactional
class JudgmentEventServiceIntTest extends WireMockIntTestBase {

    private static final String SERVICE_ID = "IT01";
    private static final String SERVICE_ID_UNRECOGNISED = "IT99";
    private static final String COURT_EPIMS_ID = "123456";
    private static final String COURT_EPIMS_ID_UNRECOGNISED = "999999";
    private static final String COURT_CODE = "123";

    private static final String JUDGMENT_ID_SUFFIX_1 = "-1";
    private static final String JUDGMENT_ID_SUFFIX_2 = "-2";

    private final JudgmentEventService judgmentEventService;
    private final JudgmentRepository judgmentRepository;

    @Autowired
    public JudgmentEventServiceIntTest(JudgmentEventService judgmentEventService,
                                       JudgmentRepository judgmentRepository) {
        this.judgmentEventService = judgmentEventService;
        this.judgmentRepository = judgmentRepository;
    }

    @Test
    void testProcessJudgmentEventUnrecognisedServiceId() {
        JudgmentEvent judgmentEvent = createJudgmentEventCommon("1001",
                                                                LocalDateTime.of(2024, 1, 1, 1, 0, 0),
                                                                "10000001",
                                                                "0AA10001",
                                                                new BigDecimal("11.00"),
                                                                LocalDate.of(2024, 1, 1),
                                                                RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setServiceId(SERVICE_ID_UNRECOGNISED);
        judgmentEvent.setCourtEpimsId(COURT_EPIMS_ID);
        judgmentEvent.setDefendant1(createDefendant("Jud1Def1", "JD1 1DD"));

        assertThrows(UnrecognisedServiceIdException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "UnrecognisedServiceIdException should be thrown");
    }

    @Test
    void testProcessJudgmentEventMissingCancellationDate() {
        JudgmentEvent judgmentEvent = createJudgmentEvent("2002",
                                                          LocalDateTime.of(2024, 2, 2, 2, 0, 0),
                                                          "20000002",
                                                          "0AA20002",
                                                          new BigDecimal("22.00"),
                                                          LocalDate.of(2024, 2, 2),
                                                          RegistrationType.JUDGMENT_SATISFIED);
        judgmentEvent.setDefendant1(createDefendant("Jud2Def1", "JD2 1DD"));

        assertThrows(MissingCancellationDateException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "MissingCancellationDateException should be thrown");
    }

    @Test
    void testProcessJudgmentEventUnrecognisedEpimsId() {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseNotFound(COURT_EPIMS_ID_UNRECOGNISED);

        JudgmentEvent judgmentEvent = createJudgmentEventCommon("3003",
                                                                LocalDateTime.of(2024, 3, 3, 3, 0, 0),
                                                                "30000003",
                                                                "0AA30003",
                                                                new BigDecimal("33.00"),
                                                                LocalDate.of(2024, 3, 3),
                                                                RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setCourtEpimsId(COURT_EPIMS_ID_UNRECOGNISED);
        judgmentEvent.setDefendant1(createDefendant("Jud3Def1", "JD3 1DD"));

        assertThrows(UnrecognisedEpimsIdException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "UnrecognisedEpimsIdException should be thrown");
    }

    @Test
    void testProcessJudgmentEventUpdateExistingOneDefendant() {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        JudgmentEvent judgmentEvent = createJudgmentEvent("4004",
                                                          LocalDateTime.of(2024, 4, 4, 4, 0, 0),
                                                          "40000004",
                                                          "0AA40004",
                                                          new BigDecimal("44.00"),
                                                          LocalDate.of(2024, 4, 4),
                                                          RegistrationType.JUDGMENT_MODIFIED);
        judgmentEvent.setDefendant1(createDefendant("Jud4Def1", "JD4 1DD"));

        assertThrows(UpdateExistingJudgmentException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "UpdateExistingJudgmentException should be thrown");
    }

    @Test
    void testProcessJudgmentEventUpdateExistingTwoDefendants() {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        JudgmentEvent judgmentEvent = createJudgmentEvent("5005",
                                                          LocalDateTime.of(2024, 5, 5, 5, 0, 0),
                                                          "50000005",
                                                          "0AA50005",
                                                          new BigDecimal("55.00"),
                                                          LocalDate.of(2024, 5, 5),
                                                          RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setDefendant1(createDefendant("Jud5Def1", "JD5 1DD"));
        judgmentEvent.setDefendant2(createDefendant("DiffJud5Def2", "JD5 2DD"));

        assertThrows(UpdateExistingJudgmentException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "UpdateExistingJudgmentException should be thrown");
    }

    @Test
    void testProcessJudgmentEventDifferentNumberOfDefendants() {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        JudgmentEvent judgmentEvent = createJudgmentEvent("6006",
                                                          LocalDateTime.of(2024, 6, 6, 6, 0, 0),
                                                          "60000006",
                                                          "0AA60006",
                                                          new BigDecimal("66.00"),
                                                          LocalDate.of(2024, 6, 6),
                                                          RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setDefendant1(createDefendant("Jud6Def1", "JD6 1DD"));
        judgmentEvent.setDefendant2(createDefendant("Jud6Def2", "JD6 2DD"));

        assertThrows(DifferentNumberOfDefendantsException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "DifferentNumberOfDefendantsException should be thrown");
    }

    @Test
    void testProcessJudgmentEventDuplicate() {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        String judgmentId = "7007";
        LocalDateTime judgmentEventTimestamp = LocalDateTime.of(2024, 7, 7, 7, 0, 0);
        String caseNumber = "0AA70007";

        List<Judgment> judgmentsBefore =
            judgmentRepository.findByEventDetails(SERVICE_ID, judgmentId, judgmentEventTimestamp, caseNumber);
        assertEquals(2, judgmentsBefore.size(), "JudgmentEvent to be saved should already exist");

        JudgmentEvent judgmentEvent = createJudgmentEvent(judgmentId,
                                                          judgmentEventTimestamp,
                                                          "70000007",
                                                          caseNumber,
                                                          new BigDecimal("77.00"),
                                                          LocalDate.of(2024, 7, 7),
                                                          RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setDefendant1(createDefendant("Jud7Def1", "JD7 1DD"));
        judgmentEvent.setDefendant2(createDefendant("Jud7Def2", "JD7 2DD"));

        judgmentEventService.processJudgmentEvent(judgmentEvent);

        List<Judgment> judgmentsAfter =
            judgmentRepository.findByEventDetails(SERVICE_ID, judgmentId, judgmentEventTimestamp, caseNumber);

        assertEquals(2, judgmentsAfter.size(), "JudgmentEvent should not be saved");
        assertExistingJudgment(judgmentsBefore.get(0), judgmentsAfter.get(0));
        assertExistingJudgment(judgmentsBefore.get(1), judgmentsAfter.get(1));
    }

    @Test
    void testProcessJudgmentEventNoExisting() {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        String judgmentId = "8008";
        LocalDateTime judgmentEventTimestamp = LocalDateTime.of(2024, 8, 8, 8, 0, 0);
        String caseNumber = "0AA80008";

        List<Judgment> judgmentsBefore =
            judgmentRepository.findByEventDetails(SERVICE_ID, judgmentId, judgmentEventTimestamp, caseNumber);
        assertEquals(0, judgmentsBefore.size(), "JudgmentEvent to be saved should not already exist");

        JudgmentEvent judgmentEvent = createJudgmentEvent(judgmentId,
                                                          judgmentEventTimestamp,
                                                          "80000008",
                                                          caseNumber,
                                                          new BigDecimal("88.00"),
                                                          LocalDate.of(2024, 8, 8),
                                                          RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setDefendant1(createDefendant("Jud8Def1", "JD8 1DD"));
        judgmentEvent.setDefendant2(createDefendant("Jud8Def2", "JD8 2DD"));

        judgmentEventService.processJudgmentEvent(judgmentEvent);

        List<Judgment> judgmentsAfter =
            judgmentRepository.findByEventDetails(SERVICE_ID, judgmentId, judgmentEventTimestamp, caseNumber);

        assertEquals(2, judgmentsAfter.size(), "JudgmentEvent should be saved");
        assertNewJudgment(judgmentEvent, judgmentEvent.getDefendant1(), JUDGMENT_ID_SUFFIX_1, judgmentsAfter.get(0));
        assertNewJudgment(judgmentEvent, judgmentEvent.getDefendant2(), JUDGMENT_ID_SUFFIX_2, judgmentsAfter.get(1));
    }

    private void assertExistingJudgment(Judgment judgmentBefore, Judgment judgmentAfter) {
        assertEquals(judgmentBefore.getId(), judgmentAfter.getId(), "Before and after ids should match");
        assertEquals(judgmentBefore.getVersionNumber(),
                     judgmentAfter.getVersionNumber(),
                     "Before and after version numbers should match");
    }

    private void assertNewJudgment(JudgmentEvent judgmentEvent,
                                   Defendant defendant,
                                   String judgmentIdSuffix,
                                   Judgment judgment) {
        assertNotEquals(0, judgment.getId(), "Judgment should have an id value");
        assertEquals(0, judgment.getVersionNumber(), "Unexpected VersionNumber");
        assertEquals(judgmentEvent.getServiceId(), judgment.getServiceId(), "Unexpected ServiceId");
        assertEquals(judgmentEvent.getJudgmentId() + judgmentIdSuffix,
                     judgment.getJudgmentId(),
                     "Unexpected JudgmentId");
        assertEquals(judgmentEvent.getJudgmentEventTimeStamp(),
                     judgment.getJudgmentEventTimestamp(),
                     "Unexpected JudgmentEventTimestamp");
        assertEquals(COURT_CODE, judgment.getCourtCode(), "Unexpected CourtCode");
        assertEquals(judgmentEvent.getCcdCaseRef(), judgment.getCcdCaseRef(), "Unexpected CcdCaseRef");
        assertEquals(judgmentEvent.getCaseNumber(), judgment.getCaseNumber(), "Unexpected CaseNumber");
        assertEquals(judgmentEvent.getJudgmentAdminOrderTotal(),
                     judgment.getJudgmentAdminOrderTotal(),
                     "Unexpected JudgmentAdminOrderTotal");
        assertEquals(judgmentEvent.getJudgmentAdminOrderDate(),
                     judgment.getJudgmentAdminOrderDate(),
                     "Unexpected JudgmentAdminOrderDate");
        assertEquals(judgmentEvent.getRegistrationType().getRegType(),
                     judgment.getRegistrationType(),
                     "Unexpected RegistrationType");
        assertNull(judgment.getCancellationDate(), "CancellationDate should be null");
        assertJudgmentDefendant(defendant, judgment);
        assertNull(judgment.getReportedToRtl(), "ReportedToRtl should be null");
    }

    private void assertJudgmentDefendant(Defendant defendant, Judgment judgment) {
        assertEquals(defendant.getDefendantName(), judgment.getDefendantName(), "Unexpected DefendantName");
        assertNull(judgment.getDefendantDob(), "DefendantDob should be null");

        DefendantAddress defendantAddress = defendant.getDefendantAddress();
        assertEquals(defendantAddress.getDefendantAddressLine1(),
                     judgment.getDefendantAddressLine1(),
                     "Unexpected DefendantAddressLine1");
        assertNull(judgment.getDefendantAddressLine2(), "DefendantAddressLine2 should be null");
        assertNull(judgment.getDefendantAddressLine3(), "DefendantAddressLine3 should be null");
        assertNull(judgment.getDefendantAddressLine4(), "DefendantAddressLine4 should be null");
        assertNull(judgment.getDefendantAddressLine5(), "DefendantAddressLine5 should be null");
        assertEquals(defendantAddress.getDefendantPostcode(),
                     judgment.getDefendantAddressPostcode(),
                     "Unexpected DefendantAddressPostcode");
    }

    private JudgmentEvent createJudgmentEvent(String judgmentId,
                                              LocalDateTime judgmentEventTimeStamp,
                                              String ccdCaseRef,
                                              String caseNumber,
                                              BigDecimal judgmentAdminOrderTotal,
                                              LocalDate judgmentAdminOrderDate,
                                              RegistrationType registrationType) {

        JudgmentEvent judgmentEvent = createJudgmentEventCommon(judgmentId,
                                                                judgmentEventTimeStamp,
                                                                ccdCaseRef,
                                                                caseNumber,
                                                                judgmentAdminOrderTotal,
                                                                judgmentAdminOrderDate,
                                                                registrationType);

        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setCourtEpimsId(COURT_EPIMS_ID);

        return judgmentEvent;
    }

    private JudgmentEvent createJudgmentEventCommon(String judgmentId,
                                                    LocalDateTime judgmentEventTimestamp,
                                                    String ccdCaseRef,
                                                    String caseNumber,
                                                    BigDecimal judgmentAdminOrderTotal,
                                                    LocalDate judgmentAdminOrderDate,
                                                    RegistrationType registrationType) {
        JudgmentEvent judgmentEvent = new JudgmentEvent();

        judgmentEvent.setJudgmentId(judgmentId);
        judgmentEvent.setJudgmentEventTimeStamp(judgmentEventTimestamp);
        judgmentEvent.setCcdCaseRef(ccdCaseRef);
        judgmentEvent.setCaseNumber(caseNumber);
        judgmentEvent.setJudgmentAdminOrderTotal(judgmentAdminOrderTotal);
        judgmentEvent.setJudgmentAdminOrderDate(judgmentAdminOrderDate);
        judgmentEvent.setRegistrationType(registrationType);

        return judgmentEvent;
    }

    private Defendant createDefendant(String prefix, String postcode) {
        DefendantAddress address = new DefendantAddress();
        address.setDefendantAddressLine1(prefix + " Address Line 1");
        address.setDefendantPostcode(postcode);

        Defendant defendant = new Defendant();
        defendant.setDefendantName(prefix + "FirstName " + prefix + "LastName");
        defendant.setDefendantAddress(address);

        return defendant;
    }
}
