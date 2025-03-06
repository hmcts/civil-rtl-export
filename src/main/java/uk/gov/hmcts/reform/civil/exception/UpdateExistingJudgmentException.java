package uk.gov.hmcts.reform.civil.exception;

import java.io.Serial;

public class UpdateExistingJudgmentException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6337833734843959504L;

    public UpdateExistingJudgmentException() {
        super();
    }
}
