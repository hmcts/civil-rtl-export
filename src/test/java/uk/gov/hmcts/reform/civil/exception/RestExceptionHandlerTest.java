package uk.gov.hmcts.reform.civil.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestExceptionHandlerTest {

    private static final int HTTP_STATUS_BAD_REQUEST = 400;

    private RestExceptionHandler restExceptionHandler;

    @BeforeEach
    void setUp() {
        restExceptionHandler = new RestExceptionHandler();
    }

    @Test
    void testHandleUnrecognisedServiceIdException() {
        UnrecognisedServiceIdException exception = new UnrecognisedServiceIdException();
        ResponseEntity<Object> response = restExceptionHandler.handleUnrecognisedServiceIdException(exception);
        assertResponse(response, "001", "unrecognised serviceid");
    }

    @Test
    void testHandleUnrecognisedEpimsIdException() {
        UnrecognisedEpimsIdException exception = new UnrecognisedEpimsIdException();
        ResponseEntity<Object> response = restExceptionHandler.handleUnrecognisedEpimsIdException(exception);
        assertResponse(response, "003", "unrecognised EPIMSId");
    }

    @Test
    void testHandleMissingCancellationDateException() {
        MissingCancellationDateException exception = new MissingCancellationDateException();
        ResponseEntity<Object> response = restExceptionHandler.handleMissingCancellationDateException(exception);
        assertResponse(response, "004", "missing cancellation date");
    }

    @Test
    void testHandleUpdateExistingJudgmentException() {
        UpdateExistingJudgmentException exception = new UpdateExistingJudgmentException();
        ResponseEntity<Object> response = restExceptionHandler.handleUpdateExistingJudgmentException(exception);
        assertResponse(response, "008", "update of extant record not allowed");
    }

    @Test
    void testHandleDifferentNumberOfDefendantsException() {
        DifferentNumberOfDefendantsException exception = new DifferentNumberOfDefendantsException();
        ResponseEntity<Object> response = restExceptionHandler.handleDifferentNumberOfDefendantsException(exception);
        assertResponse(response, "009", "changing number of defendants not allowed");
    }

    private void assertResponse(ResponseEntity<Object> response,
                                String expectedErrorCode,
                                String expectedErrorMessage) {
        assertEquals(HTTP_STATUS_BAD_REQUEST,
                     response.getStatusCode().value(),
                     "Response returned by exception handler has unexpected status code");

        Object responseBody = response.getBody();
        assertNotNull(responseBody, "Response should have a body");
        assertInstanceOf(JudgmentEventError.class, responseBody, "Response body is of an unexpected type");

        JudgmentEventError error = (JudgmentEventError) responseBody;
        assertEquals(expectedErrorCode, error.getErrorCode(), "Response body has unexpected error code");
        assertEquals(expectedErrorMessage, error.getErrorMessage(), "Response body has unexpected error message");
    }
}
