package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class InvalidServiceIdException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 54748408734617573L;

    public InvalidServiceIdException() {
        super();
    }
}
