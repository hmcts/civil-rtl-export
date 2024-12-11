package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.service.validate.JudgmentEventValidatorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudgmentEventServiceTest {

    private static final String SERVICE_ID = "UT01";
    private static final String SERVICE_ID_UNRECOGNISED = "UT99";
    private static final String JUDGMENT_ID = "1001";
    private static final LocalDateTime JUDGMENT_EVENT_TIMESTAMP = LocalDateTime.of(2024, 1, 1, 1, 0, 0);
    private static final String COURT_EPIMS_ID = "123456";
    private static final String COURT_EPIMS_ID_UNRECOGNISED = "999999";
    private static final String COURT_CODE = "101";
    private static final String CCD_CASE_REF = "10000001";
    private static final String CASE_NUMBER = "0AA10001";
    private static final BigDecimal JUDGMENT_ADMIN_ORDER_TOTAL = new BigDecimal("11.00");
    private static final LocalDate JUDGMENT_ADMIN_ORDER_DATE = LocalDate.of(2024, 1, 1);
    private static final String DEFENDANT_1_NAME = "Def1FirstName Def1LastName";
    private static final String DEFENDANT_1_ADDRESS_LINE_1 = "Def1 Address Line 1";
    private static final String DEFENDANT_1_POSTCODE = "DD1 1DD";
    private static final String DEFENDANT_1_DIFF_NAME = "DiffDef1DFirstName DiffDef1LastName";
    private static final String DEFENDANT_2_NAME = "Def2FirstName Def2LastName";
    private static final String DEFENDANT_2_ADDRESS_LINE_1 = "Def2 Address Line 1";
    private static final String DEFENDANT_2_POSTCODE = "DD2 2DD";
    private static final String DEFENDANT_2_DIFF_NAME = "DiffDef2FirstName DiffDef2LastName";

    private static final String JUDGMENT_ID_SUFFIX_1 = "-1";
    private static final String JUDGMENT_ID_SUFFIX_2 = "-2";

    @Mock
    private JudgmentEventValidatorService mockJudgmentEventValidatorService;

    @Mock
    private RefDataService mockRefDataService;

    @Mock
    private JudgmentEventTransformerService mockJudgmentEventTransformerService;

    @Mock
    private JudgmentRepository mockJudgmentRepository;

    private JudgmentEventService judgmentEventService;

    @BeforeEach
    void setUp() {
        judgmentEventService = new JudgmentEventService(mockJudgmentEventValidatorService,
                                                        mockRefDataService,
                                                        mockJudgmentEventTransformerService,
                                                        mockJudgmentRepository);
    }

    @Test
    void testProcessJudgmentEventUnrecognisedServiceId() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();
        judgmentEvent.setServiceId(SERVICE_ID_UNRECOGNISED);

        UnrecognisedServiceIdException exception = new UnrecognisedServiceIdException();
        doThrow(exception).when(mockJudgmentEventValidatorService).validateServiceId(SERVICE_ID_UNRECOGNISED);

        assertThrows(UnrecognisedServiceIdException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "UnrecognisedServiceIdException should be thrown");

        verify(mockJudgmentEventValidatorService).validateServiceId(SERVICE_ID_UNRECOGNISED);
    }

    @Test
    void testProcessJudgmentEventMissingCancellationDate() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();
        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setRegistrationType(RegistrationType.ADMIN_ORDER_REVOKED);

        MissingCancellationDateException exception = new MissingCancellationDateException();
        doThrow(exception).when(mockJudgmentEventValidatorService)
            .validateCancellationDate(RegistrationType.ADMIN_ORDER_REVOKED, null);

        assertThrows(MissingCancellationDateException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "MissingCancellationDateException should be thrown");

        verify(mockJudgmentEventValidatorService).validateServiceId(SERVICE_ID);
        verify(mockJudgmentEventValidatorService).validateCancellationDate(RegistrationType.ADMIN_ORDER_REVOKED, null);
    }

    @Test
    void testProcessJudgmentEventUnrecognisedEpimsId() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();
        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setRegistrationType(RegistrationType.JUDGMENT_REGISTERED);
        judgmentEvent.setCourtEpimsId(COURT_EPIMS_ID_UNRECOGNISED);

        UnrecognisedEpimsIdException exception = new UnrecognisedEpimsIdException();
        when(mockRefDataService.getCourtLocationCode(COURT_EPIMS_ID_UNRECOGNISED)).thenThrow(exception);

        assertThrows(UnrecognisedEpimsIdException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "UnrecognisedEpimsIdException should be thrown");

        verify(mockJudgmentEventValidatorService).validateServiceId(SERVICE_ID);
        verify(mockJudgmentEventValidatorService).validateCancellationDate(RegistrationType.JUDGMENT_REGISTERED, null);
        verify(mockRefDataService).getCourtLocationCode(COURT_EPIMS_ID_UNRECOGNISED);
    }

    @Test
    void testProcessJudgmentEventNoExisting() {
        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant(DEFENDANT_1_NAME);

        List<Judgment> newJudgments = new ArrayList<>();
        Judgment newJudgment = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                              DEFENDANT_1_NAME,
                                              DEFENDANT_1_ADDRESS_LINE_1,
                                              DEFENDANT_1_POSTCODE);
        newJudgments.add(newJudgment);

        List<Judgment> existingJudgments = new ArrayList<>();

        configureMockBehaviour(judgmentEvent, newJudgments, existingJudgments);

        assertDoesNotThrow(() -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                           "No exceptions should be thrown");

        verifyMockBehaviour(judgmentEvent);
        verify(mockJudgmentRepository).saveAll(newJudgments);
    }

    @Test
    void testProcessJudgmentEventUpdatedOneDefendant() {
        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant(DEFENDANT_1_DIFF_NAME);

        List<Judgment> newJudgments = new ArrayList<>();
        Judgment newJudgment = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                              DEFENDANT_1_DIFF_NAME,
                                              DEFENDANT_1_ADDRESS_LINE_1,
                                              DEFENDANT_1_POSTCODE);
        newJudgments.add(newJudgment);

        List<Judgment> existingJudgments = new ArrayList<>();
        Judgment existingJudgment = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                                   DEFENDANT_1_NAME,
                                                   DEFENDANT_1_ADDRESS_LINE_1,
                                                   DEFENDANT_1_POSTCODE);
        existingJudgments.add(existingJudgment);

        checkUpdateExistingJudgmentException(judgmentEvent, newJudgments, existingJudgments);
    }

    @Test
    void testProcessJudgmentEventUpdatedTwoDefendants() {
        List<Judgment> newJudgments = new ArrayList<>();
        Judgment newJudgment1 = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                               DEFENDANT_1_NAME,
                                               DEFENDANT_1_ADDRESS_LINE_1,
                                               DEFENDANT_1_POSTCODE);
        newJudgments.add(newJudgment1);
        Judgment newJudgment2 = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_2,
                                               DEFENDANT_2_DIFF_NAME,
                                               DEFENDANT_2_ADDRESS_LINE_1,
                                               DEFENDANT_2_POSTCODE);
        newJudgments.add(newJudgment2);

        List<Judgment> existingJudgments = new ArrayList<>();
        Judgment existingJudgment1 = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                                    DEFENDANT_1_NAME,
                                                    DEFENDANT_1_ADDRESS_LINE_1,
                                                    DEFENDANT_1_POSTCODE);
        existingJudgments.add(existingJudgment1);
        Judgment existingJudgment2 = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_2,
                                                    DEFENDANT_2_NAME,
                                                    DEFENDANT_2_ADDRESS_LINE_1,
                                                    DEFENDANT_2_POSTCODE);
        existingJudgments.add(existingJudgment2);

        JudgmentEvent judgmentEvent = createJudgmentEventTwoDefendants();
        checkUpdateExistingJudgmentException(judgmentEvent, newJudgments, existingJudgments);
    }

    @Test
    void testProcessJudgmentEventDifferentNumberOfDefendants() {
        List<Judgment> newJudgments = new ArrayList<>();
        Judgment newJudgment = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                              DEFENDANT_1_NAME,
                                              DEFENDANT_1_ADDRESS_LINE_1,
                                              DEFENDANT_1_POSTCODE);
        newJudgments.add(newJudgment);

        List<Judgment> existingJudgments = new ArrayList<>();
        Judgment existingJudgment1 = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                                    DEFENDANT_1_NAME,
                                                    DEFENDANT_1_ADDRESS_LINE_1,
                                                    DEFENDANT_1_POSTCODE);
        existingJudgments.add(existingJudgment1);
        Judgment existingJudgment2 = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_2,
                                                    DEFENDANT_2_NAME,
                                                    DEFENDANT_2_ADDRESS_LINE_1,
                                                    DEFENDANT_2_POSTCODE);
        existingJudgments.add(existingJudgment2);

        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant(DEFENDANT_1_NAME);

        configureMockBehaviour(judgmentEvent, newJudgments, existingJudgments);

        assertThrows(DifferentNumberOfDefendantsException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "DifferentNumberOfDefendantsException should be thrown");

        verifyMockBehaviour(judgmentEvent);
    }

    @Test
    void testProcessJudgmentEventSameAsExisting() {
        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant(DEFENDANT_1_NAME);

        List<Judgment> newJudgments = new ArrayList<>();
        Judgment newJudgment = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                              DEFENDANT_1_NAME,
                                              DEFENDANT_1_ADDRESS_LINE_1,
                                              DEFENDANT_1_POSTCODE);
        newJudgments.add(newJudgment);

        List<Judgment> existingJudgments = new ArrayList<>();
        Judgment existingJudgment = createJudgment(JUDGMENT_ID + JUDGMENT_ID_SUFFIX_1,
                                                   DEFENDANT_1_NAME,
                                                   DEFENDANT_1_ADDRESS_LINE_1,
                                                   DEFENDANT_1_POSTCODE);
        existingJudgments.add(existingJudgment);

        configureMockBehaviour(judgmentEvent, newJudgments, existingJudgments);

        assertDoesNotThrow(() -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                           "No exceptions should be thrown");

        verifyMockBehaviour(judgmentEvent);
        verify(mockJudgmentRepository, never()).saveAll(newJudgments);
    }

    private void checkUpdateExistingJudgmentException(JudgmentEvent judgmentEvent,
                                                      List<Judgment> newJudgments,
                                                      List<Judgment> existingJudgments) {
        configureMockBehaviour(judgmentEvent, newJudgments, existingJudgments);

        assertThrows(UpdateExistingJudgmentException.class,
                     () -> judgmentEventService.processJudgmentEvent(judgmentEvent),
                     "UpdateExistingJudgmentException should be thrown");

        verifyMockBehaviour(judgmentEvent);
    }

    private void configureMockBehaviour(JudgmentEvent judgmentEvent,
                                        List<Judgment> newJudgments,
                                        List<Judgment> existingJudgments) {
        when(mockRefDataService.getCourtLocationCode(COURT_EPIMS_ID)).thenReturn(COURT_CODE);
        when(mockJudgmentEventTransformerService.transformJudgmentEvent(judgmentEvent, COURT_CODE))
            .thenReturn(newJudgments);
        when(mockJudgmentRepository.findByEventDetails(SERVICE_ID, JUDGMENT_ID, JUDGMENT_EVENT_TIMESTAMP, CASE_NUMBER))
            .thenReturn(existingJudgments);
    }

    private void verifyMockBehaviour(JudgmentEvent judgmentEvent) {
        verify(mockJudgmentEventValidatorService).validateServiceId(SERVICE_ID);
        verify(mockJudgmentEventValidatorService).validateCancellationDate(RegistrationType.JUDGMENT_REGISTERED, null);
        verify(mockRefDataService).getCourtLocationCode(COURT_EPIMS_ID);
        verify(mockJudgmentEventTransformerService).transformJudgmentEvent(judgmentEvent, COURT_CODE);
        verify(mockJudgmentRepository).findByEventDetails(SERVICE_ID,
                                                          JUDGMENT_ID,
                                                          JUDGMENT_EVENT_TIMESTAMP,
                                                          CASE_NUMBER);
    }

    private JudgmentEvent createJudgmentEventOneDefendant(String defendant1Name) {
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

        Defendant defendant1 = createDefendant(defendant1Name, DEFENDANT_1_ADDRESS_LINE_1, DEFENDANT_1_POSTCODE);
        judgmentEvent.setDefendant1(defendant1);

        return judgmentEvent;
    }

    private JudgmentEvent createJudgmentEventTwoDefendants() {
        JudgmentEvent judgmentEvent = createJudgmentEventOneDefendant(DEFENDANT_1_NAME);

        Defendant defendant2 = createDefendant(DEFENDANT_2_DIFF_NAME, DEFENDANT_2_ADDRESS_LINE_1, DEFENDANT_2_POSTCODE);
        judgmentEvent.setDefendant2(defendant2);

        return judgmentEvent;
    }

    private Defendant createDefendant(String name, String addressLine1, String postcode) {
        DefendantAddress address = new DefendantAddress();
        address.setDefendantAddressLine1(addressLine1);
        address.setDefendantPostcode(postcode);

        Defendant defendant = new Defendant();
        defendant.setDefendantName(name);
        defendant.setDefendantAddress(address);

        return defendant;
    }

    private Judgment createJudgment(String judgmentId,
                                    String defendantName,
                                    String defendantAddressLine1,
                                    String defendantPostcode) {
        Judgment judgment = new Judgment();

        judgment.setServiceId(SERVICE_ID);
        judgment.setJudgmentId(judgmentId);
        judgment.setJudgmentEventTimestamp(JUDGMENT_EVENT_TIMESTAMP);
        judgment.setCourtCode(COURT_CODE);
        judgment.setCcdCaseRef(CCD_CASE_REF);
        judgment.setCaseNumber(CASE_NUMBER);
        judgment.setJudgmentAdminOrderTotal(JUDGMENT_ADMIN_ORDER_TOTAL);
        judgment.setJudgmentAdminOrderDate(JUDGMENT_ADMIN_ORDER_DATE);
        judgment.setRegistrationType(RegistrationType.JUDGMENT_REGISTERED.getRegType());
        judgment.setDefendantName(defendantName);
        judgment.setDefendantAddressLine1(defendantAddressLine1);
        judgment.setDefendantAddressPostcode(defendantPostcode);

        return judgment;
    }
}
