package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CourtVenue {

    @JsonProperty("epims_id")
    private String epimsId;

    @JsonProperty("court_type_id")
    private int courtTypeId;

    @JsonProperty("court_location_code")
    private String courtLocationCode;

    @JsonProperty("is_case_management_location")
    private String isCaseManagementLocation;
}
