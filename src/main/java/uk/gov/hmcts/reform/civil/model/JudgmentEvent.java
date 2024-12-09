package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class JudgmentEvent {

    private String serviceId;

    private String judgmentId;

    private LocalDateTime judgmentEventTimeStamp;

    private String courtEpimsId;

    private String ccdCaseRef;

    private String caseNumber;

    private BigDecimal judgmentAdminOrderTotal;

    private LocalDate judgmentAdminOrderDate;

    private RegistrationType registrationType;

    private LocalDate cancellationDate;

    private Defendant defendant1;

    private Defendant defendant2;
}
