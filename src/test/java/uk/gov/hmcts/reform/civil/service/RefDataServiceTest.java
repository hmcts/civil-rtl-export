package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.client.RefDataLocationApi;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedEpimsIdException;
import uk.gov.hmcts.reform.civil.model.CourtVenue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefDataServiceTest {

    private static final String COURT_EPIMS_ID = "123456";
    private static final String COURT_EPIMS_ID_UNRECOGNISED = "999999";
    private static final String COURT_LOCATION_CODE = "123";

    private static final int COURT_TYPE_COUNTY = 10;
    private static final String IS_CASE_MANAGEMENT_LOCATION = "Y";

    @Mock
    private RefDataLocationApi mockRefDataLocationApi;

    private RefDataService refDataService;

    @BeforeEach
    void setUp() {
        refDataService = new RefDataService(mockRefDataLocationApi);
    }

    @ParameterizedTest
    @MethodSource("venuesForUnrecognisedEpimsId")
    void testGetCourtLocationCodeUnrecognisedEpimsId(List<CourtVenue> courtVenues) {
        when(mockRefDataLocationApi
                 .getCourtVenues(COURT_EPIMS_ID_UNRECOGNISED, COURT_TYPE_COUNTY, IS_CASE_MANAGEMENT_LOCATION))
            .thenReturn(courtVenues);

        assertThrows(UnrecognisedEpimsIdException.class,
                     () -> refDataService.getCourtLocationCode(COURT_EPIMS_ID_UNRECOGNISED),
                     "UnrecognisedEpimsIdException should be thrown");

        verify(mockRefDataLocationApi)
            .getCourtVenues(COURT_EPIMS_ID_UNRECOGNISED, COURT_TYPE_COUNTY, IS_CASE_MANAGEMENT_LOCATION);
    }

    @ParameterizedTest
    @MethodSource("venuesForRecognisedEpimsId")
    void testGetCourtLocationCodeRecognisedEpimsId(List<CourtVenue> courtVenues) {
        when(mockRefDataLocationApi
                 .getCourtVenues(COURT_EPIMS_ID, COURT_TYPE_COUNTY, IS_CASE_MANAGEMENT_LOCATION))
            .thenReturn(courtVenues);

        String result = refDataService.getCourtLocationCode(COURT_EPIMS_ID);
        assertEquals(COURT_LOCATION_CODE, result, "Unexpected court location code returned");

        verify(mockRefDataLocationApi).getCourtVenues(COURT_EPIMS_ID, COURT_TYPE_COUNTY, IS_CASE_MANAGEMENT_LOCATION);
    }

    private static Stream<Arguments> venuesForUnrecognisedEpimsId() {
        List<CourtVenue> noCourtVenues = new ArrayList<>();

        List<CourtVenue> nullCourtLocationCodeVenues = new ArrayList<>();
        nullCourtLocationCodeVenues.add(createCourtVenue(COURT_EPIMS_ID_UNRECOGNISED, null));

        List<CourtVenue> blankCourtLocationCodeVenues = new ArrayList<>();
        blankCourtLocationCodeVenues.add(createCourtVenue(COURT_EPIMS_ID_UNRECOGNISED, ""));

        return Stream.of(
            arguments(named("No court venues", noCourtVenues)),
            arguments(named("Null court location code", nullCourtLocationCodeVenues)),
            arguments(named("Blank court location code", blankCourtLocationCodeVenues))
        );
    }

    private static Stream<Arguments> venuesForRecognisedEpimsId() {
        List<CourtVenue> courtVenueSingle = new ArrayList<>();
        courtVenueSingle.add(createCourtVenue(COURT_EPIMS_ID, COURT_LOCATION_CODE));

        List<CourtVenue> courtVenuesMultiple = new ArrayList<>();
        courtVenuesMultiple.add(createCourtVenue(COURT_EPIMS_ID, COURT_LOCATION_CODE));
        courtVenuesMultiple.add(createCourtVenue(COURT_EPIMS_ID, "456"));

        return Stream.of(
            arguments(named("Single court venue", courtVenueSingle)),
            arguments(named("Multiple court venues", courtVenuesMultiple))
        );
    }

    private static CourtVenue createCourtVenue(String epimsId, String courtLocationCode) {
        CourtVenue courtVenue = new CourtVenue();

        courtVenue.setEpimsId(epimsId);
        courtVenue.setCourtLocationCode(courtLocationCode);

        return courtVenue;
    }
}
