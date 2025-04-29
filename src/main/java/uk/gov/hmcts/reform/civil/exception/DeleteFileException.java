package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class DeleteFileException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8870143491929734680L;

    public DeleteFileException(String message) {
        super(message);
    }
}
