package uk.gov.hmcts.reform.civil;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import uk.gov.hmcts.reform.civil.client.RefDataLocationApi;

import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@ActiveProfiles("itest")
@EnableWireMock({
    @ConfigureWireMock(name = "ref-data-location", port = 8991),
    @ConfigureWireMock(name = "s2s-auth", port = 4502),
    @ConfigureWireMock(name = "idam", port = 5000)
})
// DirtiesContext needed so that WireMock servers are shutdown and restarted for each test class
@DirtiesContext
public abstract class WireMockIntTestBase {

    private static final String ENDPOINT_IDAM_TOKEN = "/o/token";
    private static final String ENDPOINT_S2S_LEASE = "/lease";

    private static final String COURT_TYPE_COUNTY_COURT = "10";

    @InjectWireMock("idam")
    private WireMockServer idamServer;

    @InjectWireMock("s2s-auth")
    private WireMockServer s2sAuthServer;

    @InjectWireMock("ref-data-location")
    private WireMockServer refDataLocationServer;

    public void stubIdamResponseOk() {
        String idamResponse = """
            {
              "access_token": "dummyUserToken",
              "token_type": "Bearer"
            }
            """;
        idamServer.stubFor(post(urlPathEqualTo(ENDPOINT_IDAM_TOKEN)).willReturn(okJson(idamResponse)));
    }

    public void stubS2SServiceAuthResponseOk() {
        // Using LocalDateTime.now().getNano() as a way of generating a random secret for signing JWT
        String authJwt = JWT.create().sign(Algorithm.HMAC256(String.valueOf(LocalDateTime.now().getNano())));
        s2sAuthServer.stubFor(post(urlPathEqualTo(ENDPOINT_S2S_LEASE)).willReturn(ok(authJwt)));
    }

    /**
     * Convenience method for stubbing OK responses from both Idam and S2SAuth.
     */
    public void stubIdamS2SAuthResponseOk() {
        stubIdamResponseOk();
        stubS2SServiceAuthResponseOk();
    }

    public void stubRefDataLocationApiResponseOk(String epimsId) {
        String responseTemplate = """
            [
              {
                "epims_id": "%s",
                "court_type_id": 10,
                "court_location_code": "123",
                "is_case_management_location": "Y"
              }
            ]
            """;
        String response = String.format(responseTemplate, epimsId);
        stubRefDataLocationApiResponseOk(epimsId, response);
    }

    public void stubRefDataLocationApiResponseOk(String epimsId, String response) {
        refDataLocationServer.stubFor(createMappingBuilder(epimsId).willReturn(okJson(response)));
    }

    public void stubRefDataLocationApiResponseNotFound(String epimsId) {
        refDataLocationServer.stubFor(createMappingBuilder(epimsId).willReturn(notFound()));
    }

    public void verifyStubIdam() {
        idamServer.verify(1, postRequestedFor(urlPathEqualTo(ENDPOINT_IDAM_TOKEN)));
    }

    public void verifyStubRefDataLocationApi(String epimsId) {
        refDataLocationServer.verify(1, getRequestedFor(urlPathEqualTo(RefDataLocationApi.ENDPOINT_COURT_VENUES))
            .withQueryParam(RefDataLocationApi.PARAM_EPIMS_ID, equalTo(epimsId))
            .withQueryParam(RefDataLocationApi.PARAM_COURT_TYPE_ID, equalTo(COURT_TYPE_COUNTY_COURT))
            .withQueryParam(RefDataLocationApi.PARAM_IS_CASE_MANAGEMENT_LOCATION, equalTo("Y")));
    }

    private MappingBuilder createMappingBuilder(String epimsId) {
        return get(urlPathEqualTo(RefDataLocationApi.ENDPOINT_COURT_VENUES))
            .withQueryParam(RefDataLocationApi.PARAM_EPIMS_ID, equalTo(epimsId))
            .withQueryParam(RefDataLocationApi.PARAM_COURT_TYPE_ID, equalTo(COURT_TYPE_COUNTY_COURT))
            .withQueryParam(RefDataLocationApi.PARAM_IS_CASE_MANAGEMENT_LOCATION, equalTo("Y"));
    }
}
