package uk.gov.hmcts.reform.civil.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskRunnerTest {
    private static final String TASK_NAME_KNOWN = "KnownTask";
    private static final String BEAN_NAME_KNOWN = "knownTask";

    private static final String TASK_NAME_UNKNOWN = "UnknownTask";
    private static final String BEAN_NAME_UNKNOWN = "unknownTask";

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private Runnable mockRunnable;

    private ScheduledTaskRunner scheduledTaskRunner;

    @BeforeEach
    void setUp() {
        scheduledTaskRunner = new ScheduledTaskRunner(mockApplicationContext);
    }

    @Test
    void testTaskKnown() {
        when(mockApplicationContext.getBean(BEAN_NAME_KNOWN)).thenReturn(mockRunnable);

        scheduledTaskRunner.run(TASK_NAME_KNOWN);

        verify(mockRunnable).run();
    }

    @Test
    void testTaskUnknown() {
        when(mockApplicationContext.getBean(BEAN_NAME_UNKNOWN))
            .thenThrow(new NoSuchBeanDefinitionException(BEAN_NAME_UNKNOWN));

        scheduledTaskRunner.run(TASK_NAME_UNKNOWN);

        verify(mockRunnable, never()).run();
    }
}
