package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import uk.gov.hmcts.reform.civil.service.task.ScheduledTaskRunner;

import java.io.IOException;

@JsonTest(includeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = ScheduledTaskRunner.class)
)
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
        courtVenue.setEpimsId("456789");
        courtVenue.setCourtTypeId(10);
        courtVenue.setCourtLocationCode("100");
        courtVenue.setIsCaseManagementLocation("Y");
        return courtVenue;
    }
}
