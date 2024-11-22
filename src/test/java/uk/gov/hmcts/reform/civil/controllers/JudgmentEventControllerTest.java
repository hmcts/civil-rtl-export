package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.service.JudgmentEventService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JudgmentEventControllerTest {

    private static final int HTTP_STATUS_CREATED = 201;

    @Mock
    private JudgmentEventService mockJudgmentEventService;

    private JudgmentEventController judgmentEventController;

    @BeforeEach
    void setUp() {
        judgmentEventController = new JudgmentEventController(mockJudgmentEventService);
    }

    @Test
    void testJudgmentEvent() {
        JudgmentEvent judgmentEvent = new JudgmentEvent();

        ResponseEntity<Void> response = judgmentEventController.judgmentEvent(judgmentEvent);

        assertEquals(HTTP_STATUS_CREATED, response.getStatusCode().value(), "Response has unexpected status");
        assertNull(response.getBody(), "Response body should be null");

        verify(mockJudgmentEventService).processJudgmentEvent(judgmentEvent);
    }
}
