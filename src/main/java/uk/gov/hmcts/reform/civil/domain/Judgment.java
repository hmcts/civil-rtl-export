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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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

    public boolean equalsJudgment(Judgment judgment) {
        return (
            Objects.equals(serviceId, judgment.getServiceId())
                && Objects.equals(judgmentId, judgment.getJudgmentId())
                && Objects.equals(judgmentEventTimestamp, judgment.getJudgmentEventTimestamp())
                && Objects.equals(courtCode, judgment.getCourtCode())
                && Objects.equals(ccdCaseRef, judgment.getCcdCaseRef())
                && Objects.equals(caseNumber, judgment.getCaseNumber())
                && Objects.equals(judgmentAdminOrderTotal, judgment.getJudgmentAdminOrderTotal())
                && Objects.equals(judgmentAdminOrderDate, judgment.getJudgmentAdminOrderDate())
                && Objects.equals(registrationType, judgment.getRegistrationType())
                && Objects.equals(cancellationDate, judgment.getCancellationDate())
                && Objects.equals(defendantName, judgment.getDefendantName())
                && Objects.equals(defendantAddressLine1, judgment.getDefendantAddressLine1())
                && Objects.equals(defendantAddressLine2, judgment.getDefendantAddressLine2())
                && Objects.equals(defendantAddressLine3, judgment.getDefendantAddressLine3())
                && Objects.equals(defendantAddressLine4, judgment.getDefendantAddressLine4())
                && Objects.equals(defendantAddressLine5, judgment.getDefendantAddressLine5())
                && Objects.equals(defendantAddressPostcode, judgment.getDefendantAddressPostcode())
                && Objects.equals(defendantDob, judgment.getDefendantDob())
                && Objects.equals(reportedToRtl, judgment.getReportedToRtl())
            );
    }
}
