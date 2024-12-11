package uk.gov.hmcts.reform.civil.client;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.exception.UnrecognisedEpimsIdException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RefDataLocationApiErrorDecoderTest {

    private static final int STATUS_NOT_FOUND = 404;
    private static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    private static final String METHOD_KEY = "testRefDataLocationMethodKey";
    private static final String REQUEST_URL = "testRefDataLocationUrl";

    private RefDataLocationApiErrorDecoder refDataLocationApiErrorDecoder;

    @BeforeEach
    void setUp() {
        refDataLocationApiErrorDecoder = new RefDataLocationApiErrorDecoder();
    }

    @Test
    void testDecodeStatusNotFound() {
        Response response = createResponse(STATUS_NOT_FOUND);

        Exception exception = refDataLocationApiErrorDecoder.decode(METHOD_KEY, response);
        assertInstanceOf(UnrecognisedEpimsIdException.class,
                         exception,
                         "An UnrecognisedEpimsIdException should be returned");
    }

    @Test
    void testDecodeStatusOther() {
        Response response = createResponse(STATUS_INTERNAL_SERVER_ERROR);

        Exception exception = refDataLocationApiErrorDecoder.decode(METHOD_KEY, response);
        assertInstanceOf(FeignException.class,
                         exception,
                         "A FeignException should be returned");
        FeignException feignException = (FeignException) exception;
        assertEquals(STATUS_INTERNAL_SERVER_ERROR, feignException.status(), "FeignException has unexpected status");
    }

    private Response createResponse(int status) {
        Map<String, Collection<String>> headers = new HashMap<>();
        Request request = Request.create(Request.HttpMethod.POST, REQUEST_URL, headers, null, null, null);

        return Response.builder().status(status).request(request).build();
    }
}
