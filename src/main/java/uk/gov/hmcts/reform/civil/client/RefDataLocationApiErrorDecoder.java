package uk.gov.hmcts.reform.civil.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedEpimsIdException;

public class RefDataLocationApiErrorDecoder implements ErrorDecoder {

    private static final int STATUS_NOT_FOUND = 404;

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == STATUS_NOT_FOUND) {
            return new UnrecognisedEpimsIdException();
        } else {
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
