package uk.gov.hmcts.reform.civil.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JudgmentTest {

    private static final int LINE_LENGTH = 300;

    @Test
    void testToFormattedStringMax() {

        Judgment judgment = new Judgment();

        //sample test data
        judgment.setCourtCode("123");
        judgment.setCaseNumber("CASE1234");
        judgment.setJudgmentAdminOrderTotal(BigDecimal.valueOf(99999999.99));
        judgment.setJudgmentAdminOrderDate(LocalDate.of(2024, 10, 24));
        judgment.setRegistrationType("R");
        judgment.setCancellationDate(LocalDate.of(2024, 10, 28));
        judgment.setDefendantName("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        judgment.setDefendantAddressLine1("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        judgment.setDefendantAddressLine2("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
        judgment.setDefendantAddressLine3("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        judgment.setDefendantAddressLine4("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        judgment.setDefendantAddressLine5("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
        judgment.setDefendantAddressPostcode("L2B Q5NX");
        judgment.setDefendantDob(LocalDate.of(2024, 10, 22));

        // Expected formatted string
        String expectedOutput = "123"
            + "CASE1234"
            + "99999999.99"
            + "24102024"
            + "R"
            + "28102024"
            + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
            + "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
            + "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"
            + "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD"
            + "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE"
            + "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
            + "L2B Q5NX"
            + "22102024";

        String actualOutput = judgment.toFormattedString();

        //asserting that formatted string output matches expected output
        assertEquals(expectedOutput, actualOutput, "toFormattedString does not match expected value");
        assertEquals(LINE_LENGTH, actualOutput.length(), "toFormattedString does not have expected length");
    }

    @Test
    void testToFormattedStringMin() {

        Judgment judgment = new Judgment();

        //sample test data
        judgment.setCourtCode("123");
        judgment.setCaseNumber("CASE1234");
        judgment.setJudgmentAdminOrderTotal(BigDecimal.valueOf(0.01));
        judgment.setJudgmentAdminOrderDate(LocalDate.of(2024, 10, 24));
        judgment.setRegistrationType("R");
        judgment.setCancellationDate(null);
        judgment.setDefendantName("A");
        judgment.setDefendantAddressLine1("B");
        judgment.setDefendantAddressLine2(null);
        judgment.setDefendantAddressLine3(null);
        judgment.setDefendantAddressLine4(null);
        judgment.setDefendantAddressLine5(null);
        judgment.setDefendantAddressPostcode("L2B Q5NX");
        judgment.setDefendantDob(null);

        // Expected formatted string
        String expectedOutput = "123"
            + "CASE1234"
            + "00000000.01"
            + "24102024"
            + "R"
            + "        "
            + "A                                                                     "
            + "B                                  "
            + "                                   "
            + "                                   "
            + "                                   "
            + "                                   "
            + "L2B Q5NX"
            + "        ";

        String actualOutput = judgment.toFormattedString();

        //asserting that formatted string output matches expected output
        assertEquals(expectedOutput, actualOutput, "toFormattedString does not match expected value");
        assertEquals(LINE_LENGTH, actualOutput.length(), "toFormattedString does not have expected length");
    }

    @Test
    void testToFormattedStringAddressLines() {

        Judgment judgment = new Judgment();

        //sample test data
        judgment.setCourtCode("123");
        judgment.setCaseNumber("CASE1234");
        judgment.setJudgmentAdminOrderTotal(BigDecimal.valueOf(0.01));
        judgment.setJudgmentAdminOrderDate(LocalDate.of(2024, 10, 24));
        judgment.setRegistrationType("R");
        judgment.setCancellationDate(null);
        judgment.setDefendantName("A");
        judgment.setDefendantAddressLine1("B");
        judgment.setDefendantAddressLine2("C");
        judgment.setDefendantAddressLine3("D");
        judgment.setDefendantAddressLine4("E");
        judgment.setDefendantAddressLine5("F");
        judgment.setDefendantAddressPostcode("L2B Q5NX");
        judgment.setDefendantDob(null);

        // Expected formatted string
        String expectedOutput = "123"
            + "CASE1234"
            + "00000000.01"
            + "24102024"
            + "R"
            + "        "
            + "A                                                                     "
            + "B                                  "
            + "C                                  "
            + "D                                  "
            + "E                                  "
            + "F                                  "
            + "L2B Q5NX"
            + "        ";

        String actualOutput = judgment.toFormattedString();

        //asserting that formatted string output matches expected output
        assertEquals(expectedOutput, actualOutput, "toFormattedString does not match expected value");
        assertEquals(LINE_LENGTH, actualOutput.length(), "toFormattedString does not have expected length");
    }


}
