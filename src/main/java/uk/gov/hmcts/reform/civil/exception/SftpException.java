package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class SftpException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7689661376348234425L;

    public SftpException(Throwable cause) {
        super(cause);
    }
}
