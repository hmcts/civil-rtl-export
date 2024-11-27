package uk.gov.hmcts.reform.civil.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {UnrecognisedServiceIdException.class})
    public ResponseEntity<Object> handleUnrecognisedServiceIdException(UnrecognisedServiceIdException e) {
        return createJudgmentEventErrorResponse("001", "unrecognised serviceid");
    }

    @ExceptionHandler(value = {UnrecognisedEpimsIdException.class})
    public ResponseEntity<Object> handleUnrecognisedEpimsIdException(UnrecognisedEpimsIdException e) {
        return createJudgmentEventErrorResponse("003", "unrecognised EPIMSId");
    }

    @ExceptionHandler(value = {MissingCancellationDateException.class})
    public ResponseEntity<Object> handleMissingCancellationDateException(MissingCancellationDateException e) {
        return createJudgmentEventErrorResponse("004", "missing cancellation date");
    }

    @ExceptionHandler(value = {UpdateExistingJudgmentException.class})
    public ResponseEntity<Object> handleUpdateExistingJudgmentException(UpdateExistingJudgmentException e) {
        return createJudgmentEventErrorResponse("008", "update of extant record not allowed");
    }

    @ExceptionHandler(value = {DifferentNumberOfDefendantsException.class})
    public ResponseEntity<Object> handleDifferentNumberOfDefendantsException(DifferentNumberOfDefendantsException e) {
        return createJudgmentEventErrorResponse("009", "changing number of defendants not allowed");
    }

    private ResponseEntity<Object> createJudgmentEventErrorResponse(String errorCode, String errorMessage) {
        JudgmentEventError judgmentEventError = new JudgmentEventError(errorCode, errorMessage);
        return ResponseEntity.badRequest().body(judgmentEventError);
    }
}
