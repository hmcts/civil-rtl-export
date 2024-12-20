package uk.gov.hmcts.reform.civil.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JudgmentEventError {

    private String errorCode;

    private String errorMessage;
}
