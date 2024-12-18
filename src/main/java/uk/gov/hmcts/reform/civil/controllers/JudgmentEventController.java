package uk.gov.hmcts.reform.civil.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.model.JudgmentEvent;
import uk.gov.hmcts.reform.civil.service.JudgmentEventService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class JudgmentEventController {

    private static final int HTTP_STATUS_CREATED = 201;

    private final JudgmentEventService judgmentEventService;

    @Autowired
    public JudgmentEventController(JudgmentEventService judgmentEventService) {
        this.judgmentEventService = judgmentEventService;
    }

    // TODO: add ApiResponse annotations for OpenAPI documentation?
    @PostMapping(value = "/judgment", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> judgmentEvent(@RequestBody JudgmentEvent judgmentEvent) {
        judgmentEventService.processJudgmentEvent(judgmentEvent);
        return ResponseEntity.status(HTTP_STATUS_CREATED).build();
    }
}
