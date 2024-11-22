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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("itest")
class JudgmentEventControllerIntTest {

    private static final String ENDPOINT_JUDGMENT = "/judgment";

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
    void testValidJudgmentEvent() throws Exception {
        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "200",
              "judgmentEventTimestamp": "2024-11-19T12:00:00Z",
              "courtEpimsId": "111111",
              "ccdCaseRef": "87654321",
              "caseNumber": "B0NN5732",
              "judgmentAdminOrderTotal": 30.00,
              "judgmentAdminOrderDate": "2024-11-01",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Def1FirstName Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Def1 Line 1",
                  "defendantAddressPostcode": "DD1 1DD"
                }
              }
            }
            """;

        invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_CREATED);
    }

    @Test
    void testInvalidServiceId() throws Exception {
        String judgmentEvent = """
            {
              "serviceId": "1234",
              "judgmentId": "200",
              "judgmentEventTimestamp": "2024-11-19T12:00:00Z",
              "courtEpimsId": "111111",
              "ccdCaseRef": "87654321",
              "caseNumber": "B0NN5732",
              "judgmentAdminOrderTotal": 30.00,
              "judgmentAdminOrderDate": "2024-11-01",
              "registrationType": "R",
              "defendant1": {
                "defendantName": "Def1FirstName Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Def1 Line 1",
                  "defendantAddressPostcode": "DD1 1DD"
                }
              }
            }
            """;

        MvcResult mvcResult = invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_BAD_REQUEST);
        JsonNode rootNode = parseMvcResult(mvcResult);

        assertJudgmentErrorResponse(rootNode, "001", "unrecognised serviceid");
    }

    @Test
    void testMissingCancellationDate() throws Exception {
        String judgmentEvent = """
            {
              "serviceId": "IT01",
              "judgmentId": "200",
              "judgmentEventTimestamp": "2024-11-19T12:00:00Z",
              "courtEpimsId": "111111",
              "ccdCaseRef": "87654321",
              "caseNumber": "B0NN5732",
              "judgmentAdminOrderTotal": 30.00,
              "judgmentAdminOrderDate": "2024-11-01",
              "registrationType": "C",
              "defendant1": {
                "defendantName": "Def1FirstName Def1LastName",
                "defendantAddress": {
                  "defendantAddressLine1": "Def1 Line 1",
                  "defendantAddressPostcode": "DD1 1DD"
                }
              }
            }
            """;

        MvcResult mvcResult = invokeJudgmentEndpoint(judgmentEvent, HTTP_STATUS_BAD_REQUEST);
        JsonNode rootNode = parseMvcResult(mvcResult);

        assertJudgmentErrorResponse(rootNode, "004", "missing cancellation date");
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
