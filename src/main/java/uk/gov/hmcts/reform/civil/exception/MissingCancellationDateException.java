package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class MissingCancellationDateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4852197966357698967L;

    public MissingCancellationDateException() {
        super();
    }
}
