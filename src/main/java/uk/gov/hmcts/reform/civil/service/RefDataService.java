package uk.gov.hmcts.reform.civil.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.client.RefDataLocationApi;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedEpimsIdException;
import uk.gov.hmcts.reform.civil.model.CourtVenue;

import java.util.List;

@Service
public class RefDataService {

    private static final int COURT_TYPE_COUNTY = 10;
    private static final String IS_COURT_MANAGEMENT_LOCATION = "Y";

    private final RefDataLocationApi refDataLocationApi;

    @Autowired
    public RefDataService(RefDataLocationApi refDataLocationApi) {
        this.refDataLocationApi = refDataLocationApi;
    }

    public String getCourtLocationCode(String courtEpimsId) {
        String courtLocationCode;

        List<CourtVenue> courtVenues = getCourtVenues(courtEpimsId);

        if (courtVenues.isEmpty()) {
            throw new UnrecognisedEpimsIdException();
        } else {
            CourtVenue venue = courtVenues.getFirst();
            courtLocationCode = venue.getCourtLocationCode() != null ? venue.getCourtLocationCode() : "";
        }

        if (courtLocationCode.isEmpty()) {
            throw new UnrecognisedEpimsIdException();
        }

        return courtLocationCode;
    }

    private List<CourtVenue> getCourtVenues(String courtEpimsId) {
        return refDataLocationApi.getCourtVenues(courtEpimsId,
                                                 COURT_TYPE_COUNTY,
                                                 IS_COURT_MANAGEMENT_LOCATION);
    }
}
