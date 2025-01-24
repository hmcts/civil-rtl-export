package uk.gov.hmcts.reform.civil.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Table(name = "JUDGMENTS")
@Entity
@Getter
@Setter
public class Judgment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jud_seq")
    @SequenceGenerator(name = "jud_seq", sequenceName = "jud_seq", allocationSize = 1)
    @Column(name = "ID")
    private long id;

    @Version
    @Column(name = "VERSION_NUMBER")
    private int versionNumber;

    @Column(name = "SERVICE_ID")
    private String serviceId;

    @Column(name = "JUDGMENT_ID")
    private String judgmentId;

    @Column(name = "JUDGMENT_EVENT_TIMESTAMP")
    private LocalDateTime judgmentEventTimestamp;

    @Column(name = "COURT_CODE")
    private String courtCode;

    @Column(name = "CCD_CASE_REF")
    private String ccdCaseRef;

    @Column(name = "CASE_NUMBER")
    private String caseNumber;

    @Column(name = "JUDGMENT_ADMIN_ORDER_TOTAL")
    private BigDecimal judgmentAdminOrderTotal;

    @Column(name = "JUDGMENT_ADMIN_ORDER_DATE")
    private LocalDate judgmentAdminOrderDate;

    @Column(name = "REGISTRATION_TYPE")
    private String registrationType;

    @Column(name = "CANCELLATION_DATE")
    private LocalDate cancellationDate;

    @Column(name = "DEFENDANT_NAME")
    private String defendantName;

    @Column(name = "DEFENDANT_ADDRESS_LINE_1")
    private String defendantAddressLine1;

    @Column(name = "DEFENDANT_ADDRESS_LINE_2")
    private String defendantAddressLine2;

    @Column(name = "DEFENDANT_ADDRESS_LINE_3")
    private String defendantAddressLine3;

    @Column(name = "DEFENDANT_ADDRESS_LINE_4")
    private String defendantAddressLine4;

    @Column(name = "DEFENDANT_ADDRESS_LINE_5")
    private String defendantAddressLine5;

    @Column(name = "DEFENDANT_ADDRESS_POSTCODE")
    private String defendantAddressPostcode;

    @Column(name = "DEFENDANT_DOB")
    private LocalDate defendantDob;

    @Column(name = "REPORTED_TO_RTL")
    private LocalDateTime reportedToRtl;

    // toString Method
    @Override
    public String toString() {
        return "Judgments[" + "id=" + id
                + ", versionNumber=" + versionNumber
                + ", serviceId=" + serviceId
                + ", judgmentId=" + judgmentId
                + ", judgmentEventTimestamp=" + judgmentEventTimestamp
                + ", courtCode=" + courtCode
                + ", ccdCaseRef=" + ccdCaseRef
                + ", caseNumber=" + caseNumber
                + ", judgmentAdminOrderTotal=" + judgmentAdminOrderTotal
                + ", judgmentAdminOrderDate=" + judgmentAdminOrderDate
                + ", registrationType=" + registrationType
                + ", cancellationDate=" + cancellationDate
                + ", defendantName=" + defendantName
                + ", defendantAddressLine1=" + defendantAddressLine1
                + ", defendantAddressLine2=" + defendantAddressLine2
                + ", defendantAddressLine3=" + defendantAddressLine3
                + ", defendantAddressLine4=" + defendantAddressLine4
                + ", defendantAddressLine5=" + defendantAddressLine5
                + ", defendantAddressPostcode=" + defendantAddressPostcode
                + ", defendantDob=" + defendantDob
                + ", reportedToRtl=" + reportedToRtl
                + "]";
    }

    //method to generate date formatted string
    public String toFormattedString() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        //converting judgment objects into formatted strings according to the specified RTL Output File Format
        return String.join("",
                //left pad courtCode to 3 characters
                courtCode,
                //right pad caseNumber to 8 characters
                caseNumber,
                //left pad judgmentAdminOrderTotal to 11 characters with zeros "decimal(8.2)"
                StringUtils.leftPad(String.format("%08.2f", judgmentAdminOrderTotal), 11, '0'),
                //format judgmentAdminOrderDate to DDMMYYYY, left padded to 8 characters
                judgmentAdminOrderDate.format(dateTimeFormatter),
                //***** no padding, 1 character
                registrationType,
                //format cancellationDate to DDMMYYYY, or right pad with spaces if null
                cancellationDate != null ? cancellationDate.format(dateTimeFormatter) : StringUtils.rightPad("",8),
                //right pad defendantName to 70 characters
                StringUtils.rightPad(defendantName, 70),
                //right padding defendant address lines to 35 characters
                StringUtils.rightPad(defendantAddressLine1, 35),
                formattedAddressLine(defendantAddressLine2),
                formattedAddressLine(defendantAddressLine3),
                formattedAddressLine(defendantAddressLine4),
                formattedAddressLine(defendantAddressLine5),
                //right pad defendantAddressPostCode to 8 characters
                StringUtils.rightPad(defendantAddressPostcode, 8),
                //format defendantDob to DDMMYYYY or right padded with spaces if null
                defendantDob != null ? defendantDob.format(dateTimeFormatter) : StringUtils.rightPad("", 8)
        );
    }

    private String formattedAddressLine(String addressLine) {
        return StringUtils.rightPad(addressLine == null ? "" : addressLine, 35);
    }
}
