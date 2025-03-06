package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HousekeepingServiceTest {

    private HousekeepingService housekeepingService;

    @Mock
    private JudgmentRepository mockJudgmentRepository;

    @BeforeEach
    void setUp() {
        housekeepingService = new HousekeepingService(mockJudgmentRepository, 90);
    }

    @Test
    void testDeleteOldJudgments() {
        housekeepingService.deleteOldJudgments();
        verify(mockJudgmentRepository).deleteJudgmentsBefore(any(LocalDateTime.class));
    }
}
