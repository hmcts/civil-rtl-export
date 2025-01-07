package uk.gov.hmcts.reform.civil.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.exception.JudgmentEventError;
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

    @PostMapping(value = "/judgment", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create judgment record")
    @ApiResponse(responseCode = "201", description = "Judgment record created")
    @ApiResponse(responseCode = "400",
            description = "Bad request",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = JudgmentEventError.class)
            )
    )
    @ApiResponse(responseCode = "401", description = "Service authentication failed")
    @ApiResponse(responseCode = "403", description = "Service is not an authorised service")
    public ResponseEntity<Void> judgmentEvent(@RequestBody JudgmentEvent judgmentEvent) {
        judgmentEventService.processJudgmentEvent(judgmentEvent);
        return ResponseEntity.status(HTTP_STATUS_CREATED).build();
    }
}
