package uk.gov.hmcts.reform.civil.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.civil.controllers.JudgmentEventController;
import uk.gov.hmcts.reform.civil.service.JudgmentEventService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Provider("rtl")
@PactBroker(
    scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}",
    providerBranch = "${pact.provider.branch:master}"
)
@IgnoreNoPactsToVerify
@ExtendWith(MockitoExtension.class)
class RtlProviderContractTest {

    @Mock
    private JudgmentEventService judgmentEventService;

    @PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
            .matchingBranch()
            .mainBranch()
            .deployedOrReleased();
    }

    @BeforeEach
    void beforeEach(PactVerificationContext context) {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new JudgmentEventController(judgmentEventService))
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .alwaysDo(result -> result.getResponse().setContentType(APPLICATION_JSON_VALUE))
            .build();

        MockMvcTestTarget target = new MockMvcTestTarget();
        target.setMockMvc(mockMvc);
        if (context != null) {
            context.setTarget(target);
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPactInteractions(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }
}
