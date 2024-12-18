package uk.gov.hmcts.reform.civil.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.civil.config.RefDataLocationApiConfiguration;
import uk.gov.hmcts.reform.civil.model.CourtVenue;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
    name = "ref-data-location",
    url = "${ref-data.location.url}",
    configuration = RefDataLocationApiConfiguration.class
)
public interface RefDataLocationApi {

    String ENDPOINT_COURT_VENUES = "/refdata/location/court-venues";

    String PARAM_EPIMS_ID = "epimms_id";
    String PARAM_COURT_TYPE_ID = "court_type_id";
    String PARAM_IS_CASE_MANAGEMENT_LOCATION = "is_case_management_location";

    // Authorisation headers are automatically added by interceptors in idam-legacy-auth-support
    @GetMapping(path = ENDPOINT_COURT_VENUES, produces = APPLICATION_JSON_VALUE)
    List<CourtVenue> getCourtVenues(@RequestParam(PARAM_EPIMS_ID) String epimsId,
                                    @RequestParam(PARAM_COURT_TYPE_ID) int courtTypeId,
                                    @RequestParam(PARAM_IS_CASE_MANAGEMENT_LOCATION) String isCaseManagementLocation);
}
