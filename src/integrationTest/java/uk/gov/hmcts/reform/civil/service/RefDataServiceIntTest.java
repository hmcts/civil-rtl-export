package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.civil.WireMockIntTestBase;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedEpimsIdException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("itest")
class RefDataServiceIntTest extends WireMockIntTestBase {

    private static final String ERROR_UNRECOGNISED_EPIMS_ID = "UnrecognisedEpimsIdException should be thrown";

    private final RefDataService refDataService;

    @Autowired
    public RefDataServiceIntTest(RefDataService refDataService) {
        this.refDataService = refDataService;
    }

    @Test
    void testGetCourtLocationCode() {
        stubIdamS2SAuthResponseOk();

        String epimsId = "100001";
        stubRefDataLocationApiResponseOk(epimsId);

        String courtLocationCode = refDataService.getCourtLocationCode(epimsId);
        assertEquals("123", courtLocationCode, "Unexpected court location code returned");

        verifyStubIdam();
        verifyStubRefDataLocationApi(epimsId);
    }

    @Test
    void testGetCourtLocationCodeMultipleVenues() {
        stubIdamS2SAuthResponseOk();

        String epimsId = "200002";
        String refDataResponse = """
            [
              {
                "epims_id": "200002",
                "court_type_id": 10,
                "court_location_code": "456",
                "is_case_management_location": "Y"
              },
              {
                "epims_id": "200002",
                "court_type_id": 10,
                "court_location_code": "123",
                "is_case_management_location": "Y"
              }
            ]
            """;
        stubRefDataLocationApiResponseOk(epimsId, refDataResponse);

        String courtLocationCode = refDataService.getCourtLocationCode(epimsId);
        assertEquals("456", courtLocationCode, "Unexpected court location code returned");

        verifyStubIdam();
        verifyStubRefDataLocationApi(epimsId);
    }

    @Test
    void testGetCourtLocationCodeNoVenues() {
        stubIdamS2SAuthResponseOk();

        String epimsId = "300003";
        String refDataResponse = "[]";
        stubRefDataLocationApiResponseOk(epimsId, refDataResponse);

        assertThrows(
            UnrecognisedEpimsIdException.class,
            () -> refDataService.getCourtLocationCode(epimsId),
            ERROR_UNRECOGNISED_EPIMS_ID);

        verifyStubIdam();
        verifyStubRefDataLocationApi(epimsId);
    }

    @Test
    void testGetCourtLocationCodeNoCode() {
        stubIdamS2SAuthResponseOk();

        String epimsId = "400004";
        String refDataResponse = """
            [
              {
                "epims_id": "400004",
                "court_type_id": 10,
                "is_case_management_location": "Y"
              }
            ]
            """;
        stubRefDataLocationApiResponseOk(epimsId, refDataResponse);

        assertThrows(UnrecognisedEpimsIdException.class,
                     () -> refDataService.getCourtLocationCode(epimsId),
                     ERROR_UNRECOGNISED_EPIMS_ID);

        verifyStubIdam();
        verifyStubRefDataLocationApi(epimsId);
    }

    @Test
    void testGetCourtLocationCodeNotFound() {
        stubIdamS2SAuthResponseOk();

        String epimsId = "500005";
        stubRefDataLocationApiResponseNotFound(epimsId);

        assertThrows(UnrecognisedEpimsIdException.class,
                     () -> refDataService.getCourtLocationCode(epimsId),
                     ERROR_UNRECOGNISED_EPIMS_ID);

        verifyStubIdam();
        verifyStubRefDataLocationApi(epimsId);
    }
}
