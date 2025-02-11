package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class InvalidServiceIdException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1802293643633994551L;

    public InvalidServiceIdException(String message) {
        super(message);
    }
}
