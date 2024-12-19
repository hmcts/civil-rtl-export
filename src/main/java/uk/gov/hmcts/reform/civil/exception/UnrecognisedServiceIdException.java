package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class UnrecognisedServiceIdException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4555173542114066164L;

    public UnrecognisedServiceIdException() {
        super();
    }
}
