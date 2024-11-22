package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonTest
class JudgmentEventIntTest {

    private static final String SERVICE_ID = "AAA3";
    private static final String JUDGMENT_ID = "200";
    private static final LocalDateTime JUDGMENT_EVENT_TIMESTAMP = LocalDateTime.of(2024, 11, 14, 9, 4, 1);
    private static final String COURT_EPIMS_ID = "107635";
    private static final String CCD_CASE_REF = "87654321";
    private static final String CASE_NUMBER = "B0NN5732";
    private static final BigDecimal JUDGMENT_ADMIN_ORDER_TOTAL = BigDecimal.valueOf(150.23);
    private static final LocalDate JUDGMENT_ADMIN_ORDER_DATE = LocalDate.of(2024, 11, 13);
    private static final LocalDate CANCELLATION_DATE = LocalDate.of(2024, 11, 14);
    private static final String DEFENDANT_1_PREFIX = "Def1";
    private static final String DEFENDANT_1_POSTCODE = "DD1 1DD";
    private static final LocalDate DEFENDANT_1_DOB = LocalDate.of(2001, 1, 1);
    private static final String DEFENDANT_2_PREFIX = "Def2";
    private static final String DEFENDANT_2_POSTCODE = "DD2 2DD";
    private static final LocalDate DEFENDANT_2_DOB = LocalDate.of(2002, 2, 2);

    private final JacksonTester<JudgmentEvent> jacksonTester;

    @Autowired
    public JudgmentEventIntTest(JacksonTester<JudgmentEvent> jacksonTester) {
        this.jacksonTester = jacksonTester;
    }

    @Test
    void testJsonDeserialisationAllFields() throws IOException {
        ObjectContent<JudgmentEvent> judgmentEventContent = jacksonTester.read("judgment_event_all_fields.json");
        judgmentEventContent.assertThat().usingRecursiveComparison().isEqualTo(createExpectedJudgmentEvent());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"judgment_event_non_mandatory_fields_excluded.json", "judgment_event_non_mandatory_fields_null.json"}
    )
    void testJsonDeserialisationMandatoryFieldsOnly(String fileName) throws IOException {
        ObjectContent<JudgmentEvent> judgmentEventContent = jacksonTester.read(fileName);
        judgmentEventContent.assertThat().usingRecursiveComparison()
            .isEqualTo(createExpectedJudgmentEventMandatoryFieldsOnly());
    }

    private JudgmentEvent createExpectedJudgmentEvent() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();

        judgmentEvent.setServiceId(SERVICE_ID);
        judgmentEvent.setJudgmentId(JUDGMENT_ID);
        judgmentEvent.setJudgmentEventTimeStamp(JUDGMENT_EVENT_TIMESTAMP);
        judgmentEvent.setCourtEpimsId(COURT_EPIMS_ID);
        judgmentEvent.setCcdCaseRef(CCD_CASE_REF);
        judgmentEvent.setCaseNumber(CASE_NUMBER);
        judgmentEvent.setJudgmentAdminOrderTotal(JUDGMENT_ADMIN_ORDER_TOTAL);
        judgmentEvent.setJudgmentAdminOrderDate(JUDGMENT_ADMIN_ORDER_DATE);
        judgmentEvent.setRegistrationType(RegistrationType.JUDGMENT_CANCELLED);
        judgmentEvent.setCancellationDate(CANCELLATION_DATE);
        judgmentEvent.setDefendant1(createExpectedDefendant(DEFENDANT_1_PREFIX, DEFENDANT_1_POSTCODE, DEFENDANT_1_DOB));
        judgmentEvent.setDefendant2(createExpectedDefendant(DEFENDANT_2_PREFIX, DEFENDANT_2_POSTCODE, DEFENDANT_2_DOB));

        return judgmentEvent;
    }

    private Defendant createExpectedDefendant(String prefix, String postcode, LocalDate dateOfBirth) {
        Defendant defendant = new Defendant();

        defendant.setDefendantName(prefix + "FirstName " + prefix + "LastName");
        defendant.setDefendantDateOfBirth(dateOfBirth);

        String addressLinePrefix = prefix + " addr line";

        DefendantAddress defendantAddress = new DefendantAddress();
        defendantAddress.setDefendantAddressLine1(addressLinePrefix + "1");
        defendantAddress.setDefendantAddressLine2(addressLinePrefix + "2");
        defendantAddress.setDefendantAddressLine3(addressLinePrefix + "3");
        defendantAddress.setDefendantAddressLine4(addressLinePrefix + "4");
        defendantAddress.setDefendantAddressLine5(addressLinePrefix + "5");
        defendantAddress.setDefendantPostcode(postcode);

        defendant.setDefendantAddress(defendantAddress);

        return defendant;
    }

    private JudgmentEvent createExpectedJudgmentEventMandatoryFieldsOnly() {
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

        Defendant defendant = new Defendant();
        defendant.setDefendantName(DEFENDANT_1_PREFIX + "FirstName " + DEFENDANT_1_PREFIX + "LastName");

        DefendantAddress defendantAddress = new DefendantAddress();
        defendantAddress.setDefendantAddressLine1(DEFENDANT_1_PREFIX + " addr line1");
        defendantAddress.setDefendantPostcode(DEFENDANT_1_POSTCODE);

        defendant.setDefendantAddress(defendantAddress);

        judgmentEvent.setDefendant1(defendant);

        return judgmentEvent;
    }
}
