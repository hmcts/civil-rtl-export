package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class SaveFileException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7816506228144549340L;

    public SaveFileException(Throwable cause) {
        super(cause);
    }
}
