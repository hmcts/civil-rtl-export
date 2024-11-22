package uk.gov.hmcts.reform.civil.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.civil.model.RegistrationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class JudgmentTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("judgmentsSame")
    void testEqualsJudgment(Judgment judgment, Judgment otherJudgment) {
        assertTrue(judgment.equalsJudgment(otherJudgment), "Judgments should be equal");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("judgmentsFieldValues")
    void testNotEqualsJudgmentOtherFieldValueDiff(Judgment otherJudgment) {
        assertNotEqualsJudgment(otherJudgment);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("judgmentsFieldValuesNull")
    void testNotEqualsJudgmentOtherFieldValueNull(Judgment otherJudgment) {
        assertNotEqualsJudgment(otherJudgment);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("judgmentsFieldValuesNull")
    void testNotEqualsJudgmentFieldValueNull(Judgment judgment) {
        Judgment otherJudgment = createJudgment();
        assertFalse(judgment.equalsJudgment(otherJudgment), "Judgments should not be equal");
    }

    private void assertNotEqualsJudgment(Judgment otherJudgment) {
        Judgment judgment = createJudgment();
        assertFalse(judgment.equalsJudgment(otherJudgment), "Judgments should not be equal");
    }

    private static Stream<Arguments> judgmentsSame() {
        return Stream.of(
            arguments(named("allFieldsSet", createJudgment()), createJudgment()),
            arguments(named("noFieldsSet", new Judgment()), new Judgment())
        );
    }

    private static Stream<Arguments> judgmentsFieldValues() {
        return Stream.of(
            arguments(named("serviceId", createJudgmentServiceId("UT02"))),
            arguments(named("judgmentId", createJudgmentJudgmentId("judgment_id_2"))),
            arguments(named("judgmentEventTimestamp",
                            createJudgmentJudgmentEventTimestamp(LocalDateTime.of(2024, 11, 1, 2, 0, 0))),
            arguments(named("courtCode", createJudgmentCourtCode("200"))),
            arguments(named("ccdCaseRef", createJudgmentCcdCaseRef("ccd_ref_2"))),
            arguments(named("caseNumber", createJudgmentCaseNumber("casenum2"))),
            arguments(named("judgmentAdminOrderTotal",
                            createJudgmentJudgmentAdminOrderTotal(BigDecimal.valueOf(2.0))))),
            arguments(named("judgmentAdminOrderDate", createJudgmentJudgmentAdminOrderDate(LocalDate.of(2024, 10, 2)))),
            arguments(named("registrationType",
                            createJudgmentRegistrationType(RegistrationType.JUDGMENT_SATISFIED.getRegType()))),
            arguments(named("cancellationDate", createJudgmentCancellationDate(LocalDate.of(2024, 9, 2)))),
            arguments(named("defendantName", createJudgmentDefendantName("DiffDef1FirstName Def1LastName"))),
            arguments(named("defendantAddressLine1", createJudgmentDefendantAddressLine1("DiffDef1 Address Line 1"))),
            arguments(named("defendantAddressLine2", createJudgmentDefendantAddressLine2("DiffDef1 Address Line 2"))),
            arguments(named("defendantAddressLine3", createJudgmentDefendantAddressLine3("DiffDef1 Address Line 3"))),
            arguments(named("defendantAddressLine4", createJudgmentDefendantAddressLine4("DiffDef1 Address Line 4"))),
            arguments(named("defendantAddressLine1", createJudgmentDefendantAddressLine5("DiffDef1 Address Line 5"))),
            arguments(named("defendantAddressPostcode", createJudgmentDefendantAddressPostcode("DD2 2DD"))),
            arguments(named("defendantDob", createJudgmentDefendantDob(LocalDate.of(2000, 1, 2)))),
            arguments(named("reportedToRtl", createJudgmentReportedToRtl(LocalDateTime.of(2024, 11, 18, 13, 0, 0))))
        );
    }

    private static Stream<Arguments> judgmentsFieldValuesNull() {
        return Stream.of(
            arguments(named("serviceId", createJudgmentServiceId(null))),
            arguments(named("judgmentId", createJudgmentJudgmentId(null))),
            arguments(named("judgmentEventTimestamp", createJudgmentJudgmentEventTimestamp(null))),
            arguments(named("courtCode", createJudgmentCourtCode(null))),
            arguments(named("ccdCaseRef", createJudgmentCcdCaseRef(null))),
            arguments(named("caseNumber", createJudgmentCaseNumber(null))),
            arguments(named("judgmentAdminOrderTotal", createJudgmentJudgmentAdminOrderTotal(null))),
            arguments(named("judgmentAdminOrderDate", createJudgmentJudgmentAdminOrderDate(null))),
            arguments(named("registrationType", createJudgmentRegistrationType(null))),
            arguments(named("cancellationDate", createJudgmentCancellationDate(null))),
            arguments(named("defendantName", createJudgmentDefendantName(null))),
            arguments(named("defendantAddressLine1", createJudgmentDefendantAddressLine1(null))),
            arguments(named("defendantAddressLine2", createJudgmentDefendantAddressLine2(null))),
            arguments(named("defendantAddressLine3", createJudgmentDefendantAddressLine3(null))),
            arguments(named("defendantAddressLine4", createJudgmentDefendantAddressLine4(null))),
            arguments(named("defendantAddressLine1", createJudgmentDefendantAddressLine5(null))),
            arguments(named("defendantAddressPostcode", createJudgmentDefendantAddressPostcode(null))),
            arguments(named("defendantDob", createJudgmentDefendantDob(null))),
            arguments(named("reportedToRtl", createJudgmentReportedToRtl(null)))
        );
    }

    private static Judgment createJudgmentServiceId(String serviceId) {
        Judgment judgment = createJudgment();
        judgment.setServiceId(serviceId);
        return judgment;
    }

    private static Judgment createJudgmentJudgmentId(String judgmentId) {
        Judgment judgment = createJudgment();
        judgment.setJudgmentId(judgmentId);
        return judgment;
    }

    private static Judgment createJudgmentJudgmentEventTimestamp(LocalDateTime judgmentEventTimestamp) {
        Judgment judgment = createJudgment();
        judgment.setJudgmentEventTimestamp(judgmentEventTimestamp);
        return judgment;
    }

    private static Judgment createJudgmentCourtCode(String courtCode) {
        Judgment judgment = createJudgment();
        judgment.setCourtCode(courtCode);
        return judgment;
    }

    private static Judgment createJudgmentCcdCaseRef(String ccdCaseRef) {
        Judgment judgment = createJudgment();
        judgment.setCcdCaseRef(ccdCaseRef);
        return judgment;
    }

    private static Judgment createJudgmentCaseNumber(String caseNumber) {
        Judgment judgment = createJudgment();
        judgment.setCaseNumber(caseNumber);
        return judgment;
    }

    private static Judgment createJudgmentJudgmentAdminOrderTotal(BigDecimal judgmentAdminOrderTotal) {
        Judgment judgment = createJudgment();
        judgment.setJudgmentAdminOrderTotal(judgmentAdminOrderTotal);
        return judgment;
    }

    private static Judgment createJudgmentJudgmentAdminOrderDate(LocalDate judgmentAdminOrderDate) {
        Judgment judgment = createJudgment();
        judgment.setJudgmentAdminOrderDate(judgmentAdminOrderDate);
        return judgment;
    }

    private static Judgment createJudgmentRegistrationType(String registrationType) {
        Judgment judgment = createJudgment();
        judgment.setRegistrationType(registrationType);
        return judgment;
    }

    private static Judgment createJudgmentCancellationDate(LocalDate cancellationDate) {
        Judgment judgment = createJudgment();
        judgment.setCancellationDate(cancellationDate);
        return judgment;
    }

    private static Judgment createJudgmentDefendantName(String name) {
        Judgment judgment = createJudgment();
        judgment.setDefendantName(name);
        return judgment;
    }

    private static Judgment createJudgmentDefendantAddressLine1(String line1) {
        Judgment judgment = createJudgment();
        judgment.setDefendantAddressLine1(line1);
        return judgment;
    }

    private static Judgment createJudgmentDefendantAddressLine2(String line2) {
        Judgment judgment = createJudgment();
        judgment.setDefendantAddressLine2(line2);
        return judgment;
    }

    private static Judgment createJudgmentDefendantAddressLine3(String line3) {
        Judgment judgment = createJudgment();
        judgment.setDefendantAddressLine3(line3);
        return judgment;
    }

    private static Judgment createJudgmentDefendantAddressLine4(String line4) {
        Judgment judgment = createJudgment();
        judgment.setDefendantAddressLine4(line4);
        return judgment;
    }

    private static Judgment createJudgmentDefendantAddressLine5(String line5) {
        Judgment judgment = createJudgment();
        judgment.setDefendantAddressLine5(line5);
        return judgment;
    }

    private static Judgment createJudgmentDefendantAddressPostcode(String postcode) {
        Judgment judgment = createJudgment();
        judgment.setDefendantAddressPostcode(postcode);
        return judgment;
    }

    private static Judgment createJudgmentDefendantDob(LocalDate dob) {
        Judgment judgment = createJudgment();
        judgment.setDefendantDob(dob);
        return judgment;
    }

    private static Judgment createJudgmentReportedToRtl(LocalDateTime reportedToRtl) {
        Judgment judgment = createJudgment();
        judgment.setReportedToRtl(reportedToRtl);
        return judgment;
    }

    private static Judgment createJudgment() {
        Judgment judgment = new Judgment();

        judgment.setServiceId("UT01");
        judgment.setJudgmentId("judgment_id_1");
        judgment.setJudgmentEventTimestamp(LocalDateTime.of(2024, 11, 1, 1, 0, 0));
        judgment.setCourtCode("100");
        judgment.setCcdCaseRef("ccd_ref_1");
        judgment.setCaseNumber("casenum1");
        judgment.setJudgmentAdminOrderTotal(BigDecimal.valueOf(1.0));
        judgment.setJudgmentAdminOrderDate(LocalDate.of(2024, 10, 1));
        judgment.setRegistrationType(RegistrationType.JUDGMENT_CANCELLED.getRegType());
        judgment.setCancellationDate(LocalDate.of(2024, 9, 1));
        judgment.setDefendantName("Def1FirstName Def1LastName");
        judgment.setDefendantAddressLine1("Def1 Address Line 1");
        judgment.setDefendantAddressLine2("Def1 Address Line 2");
        judgment.setDefendantAddressLine3("Def1 Address Line 3");
        judgment.setDefendantAddressLine4("Def1 Address Line 4");
        judgment.setDefendantAddressLine5("Def1 Address Line 5");
        judgment.setDefendantAddressPostcode("DD1 1DD");
        judgment.setDefendantDob(LocalDate.of(2000, 1, 1));
        judgment.setReportedToRtl(LocalDateTime.of(2024, 11, 18, 12, 0, 0));

        return judgment;
    }
}
