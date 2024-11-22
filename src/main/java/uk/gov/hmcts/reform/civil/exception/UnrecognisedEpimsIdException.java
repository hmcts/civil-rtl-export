package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class UnrecognisedEpimsIdException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7372894790169157024L;

    public UnrecognisedEpimsIdException() {
        super();
    }
}
