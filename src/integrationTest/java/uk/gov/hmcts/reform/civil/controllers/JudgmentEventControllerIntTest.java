package uk.gov.hmcts.reform.civil.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.WireMockIntTestBase;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("itest")
@Sql(scripts = {"judgment_event_controller_int_test.sql"})
@Transactional
class JudgmentEventControllerIntTest extends WireMockIntTestBase {

    private static final String ENDPOINT_JUDGMENT = "/judgment";

    private static final String COURT_EPIMS_ID = "123456";
    private static final String COURT_EPIMS_ID_UNRECOGNISED = "999999";

    private static final int HTTP_STATUS_CREATED = 201;
    private static final int HTTP_STATUS_BAD_REQUEST = 400;

    private static final String FIELD_ERROR_CODE = "errorCode";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @Autowired
    public JudgmentEventControllerIntTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        objectMapper = new ObjectMapper();
    }

    @Test
    void testNoExistingJudgmentEvent() throws Exception {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "1001",
              "judgmentEventTimeStamp": "2024-01-01T01:00:00Z",
              "courtEpimsId": "123456",
              "ccdCaseRef": "10000001",
              "caseNumber": "0AA10001",
              "judgmentAdminOrderTotal": 11.00,
              "judgmentAdminOrderDate": "2024-01-01",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Jud1Def1FirstName Jud1Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud1Def1 Address Line 1",
                  "defendantPostcode": "JD1 1DD"
                }
              }
            }
            """;

        invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_CREATED);
    }

    @Test
    void testUnrecognisedServiceId() throws Exception {
        String judgmentEvent = """
            {
              "serviceId": "IT99",
              "judgmentId": "2002",
              "judgmentEventTimeStamp": "2024-02-02T02:00:00Z",
              "courtEpimsId": "123456",
              "ccdCaseRef": "20000002",
              "caseNumber": "0AA20002",
              "judgmentAdminOrderTotal": 22.00,
              "judgmentAdminOrderDate": "2024-02-02",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Jud2Def1FirstName Jud2Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud2Def1 Address Line 1",
                  "defendantPostcode": "JD2 1DD"
                }
              }
            }
            """;

        MvcResult mvcResult = invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_BAD_REQUEST);
        JsonNode rootNode = parseMvcResult(mvcResult);

        assertJudgmentErrorResponse(rootNode, "001", "unrecognised serviceid");
    }

    @Test
    void testUnrecognisedEpimsId() throws Exception {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseNotFound(COURT_EPIMS_ID_UNRECOGNISED);

        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "3003",
              "judgmentEventTimeStamp": "2024-03-03T03:00:00Z",
              "courtEpimsId": "999999",
              "ccdCaseRef": "30000003",
              "caseNumber": "0AA30003",
              "judgmentAdminOrderTotal": 33.00,
              "judgmentAdminOrderDate": "2024-03-03",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Jud3Def1FirstName Jud3Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud3Def1 Address Line 1",
                  "defendantPostcode": "JD3 1DD"
                }
              }
            }
            """;

        MvcResult mvcResult = invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_BAD_REQUEST);
        JsonNode rootNode = parseMvcResult(mvcResult);

        assertJudgmentErrorResponse(rootNode, "003", "unrecognised EPIMSId");
    }

    @Test
    void testMissingCancellationDate() throws Exception {
        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "4004",
              "judgmentEventTimeStamp": "2024-04-04T04:00:00Z",
              "courtEpimsId": "123456",
              "ccdCaseRef": "40000004",
              "caseNumber": "0AA40004",
              "judgmentAdminOrderTotal": 44.00,
              "judgmentAdminOrderDate": "2024-04-04",
              "registrationType": "C",
              "defendant1": {
                "defendantName": "Jud4Def1FirstName Jud4Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud4Def1 Address Line 1",
                  "defendantPostcode": "JD4 1DD"
                }
              }
            }
            """;

        MvcResult mvcResult = invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_BAD_REQUEST);
        JsonNode rootNode = parseMvcResult(mvcResult);

        assertJudgmentErrorResponse(rootNode, "004", "missing cancellation date");
    }

    @Test
    void testUpdateExistingJudgment() throws Exception {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "5005",
              "judgmentEventTimeStamp": "2024-05-05T05:00:00Z",
              "courtEpimsId": "123456",
              "ccdCaseRef": "50000005",
              "caseNumber": "0AA50005",
              "judgmentAdminOrderTotal": 999.00,
              "judgmentAdminOrderDate": "2024-05-05",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Jud5Def1FirstName Jud5Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud5Def1 Address Line 1",
                  "defendantPostcode": "JD5 1DD"
                }
              }
            }
            """;

        MvcResult mvcResult = invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_BAD_REQUEST);
        JsonNode rootNode = parseMvcResult(mvcResult);

        assertJudgmentErrorResponse(rootNode, "008", "update of extant record not allowed");
    }

    @Test
    void testDifferentNumberOfDefendants() throws Exception {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "6006",
              "judgmentEventTimeStamp": "2024-06-06T06:00:00Z",
              "courtEpimsId": "123456",
              "ccdCaseRef": "60000006",
              "caseNumber": "0AA60006",
              "judgmentAdminOrderTotal": 66.00,
              "judgmentAdminOrderDate": "2024-06-06",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Jud6Def1FirstName Jud6Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud6Def1 Address Line 1",
                  "defendantPostcode": "JD6 1DD"
                }
              },
              "defendant2": {
                "defendantName": "Jud6Def2FirstName Jud6Def2LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud6Def2 Address Line 1",
                  "defendantPostcode": "JD6 2DD"
                }
              }
            }
            """;

        MvcResult mvcResult = invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_BAD_REQUEST);
        JsonNode rootNode = parseMvcResult(mvcResult);

        assertJudgmentErrorResponse(rootNode, "009", "changing number of defendants not allowed");
    }

    @Test
    void testDuplicateJudgment() throws Exception {
        stubIdamS2SAuthResponseOk();
        stubRefDataLocationApiResponseOk(COURT_EPIMS_ID);

        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "7007",
              "judgmentEventTimeStamp": "2024-07-07T07:00:00Z",
              "courtEpimsId": "123456",
              "ccdCaseRef": "70000007",
              "caseNumber": "0AA70007",
              "judgmentAdminOrderTotal": 77.00,
              "judgmentAdminOrderDate": "2024-07-07",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Jud7Def1FirstName Jud7Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Jud7Def1 Address Line 1",
                  "defendantPostcode": "JD7 1DD"
                }
              }
            }
            """;

        invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_CREATED);
    }

    void assertJudgmentErrorResponse(JsonNode rootNode, String expectedErrorCode, String expectedErrorMessage) {
        JsonNode errorCodeNode = rootNode.get(FIELD_ERROR_CODE);
        assertEquals(expectedErrorCode, errorCodeNode.textValue(), "Response has unexpected error code");

        JsonNode errorMessageNode = rootNode.get(FIELD_ERROR_MESSAGE);
        assertEquals(expectedErrorMessage, errorMessageNode.textValue(), "Response has unexpected error message");
    }

    private MvcResult invokeJudgmentEndpoint(String content, int expectedStatus) throws Exception {
        return mockMvc.perform(
            post(ENDPOINT_JUDGMENT)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .content(content)
        ).andExpect(status().is(expectedStatus)).andReturn();
    }

    private JsonNode parseMvcResult(MvcResult mvcResult) throws JsonProcessingException, UnsupportedEncodingException {
        MockHttpServletResponse response = mvcResult.getResponse();
        assertNotNull(response, "Response should not be null");

        return objectMapper.readTree(response.getContentAsString());
    }
}
