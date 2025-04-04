package uk.gov.hmcts.reform.civil.service.task;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        Logger logger = (Logger) LoggerFactory.getLogger(ScheduledTaskRunner.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        scheduledTaskRunner.run(TASK_NAME_KNOWN);

        logger.detachAndStopAllAppenders();
        List<ILoggingEvent> logList = listAppender.list;

        List<String> expectedLogMessages = new ArrayList<>();
        expectedLogMessages.add("Running task [" + TASK_NAME_KNOWN + "] (bean [" + BEAN_NAME_KNOWN + "])");

        assertLogMessages(logList, expectedLogMessages);

        verify(mockRunnable).run();
    }

    @Test
    void testTaskUnknown() {
        when(mockApplicationContext.getBean(BEAN_NAME_UNKNOWN))
            .thenThrow(new NoSuchBeanDefinitionException(BEAN_NAME_UNKNOWN));

        Logger logger = (Logger) LoggerFactory.getLogger(ScheduledTaskRunner.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        scheduledTaskRunner.run(TASK_NAME_UNKNOWN);

        logger.detachAndStopAllAppenders();
        List<ILoggingEvent> logList = listAppender.list;

        List<String> expectedLogMessages = new ArrayList<>();
        expectedLogMessages.add("Error finding task");
        expectedLogMessages.add("Task not found [" + TASK_NAME_UNKNOWN + "] (bean [" + BEAN_NAME_UNKNOWN + "])");

        assertLogMessages(logList, expectedLogMessages);
    }

    private void assertLogMessages(List<ILoggingEvent> logList, List<String> expectedLogMessages) {
        int expectedNumLogMessages = expectedLogMessages.size();

        assertEquals(expectedNumLogMessages, logList.size(), "Unexpected number of messages in log");

        for (int index = 0; index < expectedNumLogMessages; index++) {
            assertEquals(expectedLogMessages.get(index),
                         logList.get(index).getFormattedMessage(),
                         "Log contains unexpected message at position " + index);
        }
    }
}
