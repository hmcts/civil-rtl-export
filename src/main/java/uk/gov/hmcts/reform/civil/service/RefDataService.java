package uk.gov.hmcts.reform.civil.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedEpimsIdException;

@Service
public class RefDataService {

    public String getCourtLocationCode(String courtEpimsId) {
        // TODO: Stub until service is implemented
        if (courtEpimsId.equals("999999")) {
            throw new UnrecognisedEpimsIdException();
        } else {
            return "123";
        }
    }
}
