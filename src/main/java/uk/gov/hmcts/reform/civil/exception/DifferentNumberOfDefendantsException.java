package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class DifferentNumberOfDefendantsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7702160129050957496L;

    public DifferentNumberOfDefendantsException() {
        super();
    }

}
