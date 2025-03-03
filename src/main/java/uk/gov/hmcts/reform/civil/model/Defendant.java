package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Defendant {

    private String defendantName;

    private DefendantAddress defendantAddress;

    private LocalDate defendantDateOfBirth;
}
