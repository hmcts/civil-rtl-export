package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;

@JsonTest
class CourtVenueIntTest {

    private final JacksonTester<CourtVenue> jacksonTester;

    @Autowired
    public CourtVenueIntTest(JacksonTester<CourtVenue> jacksonTester) {
        this.jacksonTester = jacksonTester;
    }

    @Test
    void testJsonDeserialisation() throws IOException {
        ObjectContent<CourtVenue> courtVenueContent = jacksonTester.read("court_venue.json");
        courtVenueContent.assertThat().usingRecursiveComparison().isEqualTo(createExpectedCourtVenue());
    }

    private CourtVenue createExpectedCourtVenue() {
        CourtVenue courtVenue = new CourtVenue();
        courtVenue.setCourtLocationCode("100");
        return courtVenue;
    }
}
