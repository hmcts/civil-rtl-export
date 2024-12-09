package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DefendantAddress {

    private String defendantAddressLine1;

    private String defendantAddressLine2;

    private String defendantAddressLine3;

    private String defendantAddressLine4;

    private String defendantAddressLine5;

    private String defendantPostcode;
}
