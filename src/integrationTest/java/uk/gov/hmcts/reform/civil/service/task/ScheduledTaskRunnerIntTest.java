package uk.gov.hmcts.reform.civil.service.task;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;
import uk.gov.hmcts.reform.civil.util.LocalSftpServer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.DIR_TYPE_REMOTE;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertFileNamesInDir;

@SpringBootTest
@ActiveProfiles("itest")
@Transactional
@Sql("scheduled_task_runner_int_test.sql")
class ScheduledTaskRunnerIntTest {

    private static final String TASK_NAME_SCHEDULED_REPORT = "ScheduledReportTask";
    private static final String TASK_NAME_HOUSEKEEPING = "HousekeepingTask";
    private static final String TASK_NAME_UNKNOWN = "UnknownTask";

    private static final String BEAN_NAME_UNKNOWN = "unknownTask";

    private final JudgmentRepository judgmentRepository;

    private final ScheduledTaskRunner scheduledTaskRunner;

    private final int sftpPort;
    private final String sftpRemoteDir;

    @TempDir
    private File sftpRootDir;

    @Autowired
    public ScheduledTaskRunnerIntTest(ScheduledTaskRunner scheduledTaskRunner,
                                      JudgmentRepository judgmentRepository,
                                      @Value("${rtl-export.sftp.serverPort}") int sftpPort,
                                      @Value("${rtl-export.sftp.remoteDir}") String sftpRemoteDir) {
        this.scheduledTaskRunner = scheduledTaskRunner;
        this.judgmentRepository = judgmentRepository;
        this.sftpPort = sftpPort;
        this.sftpRemoteDir = sftpRemoteDir;
    }

    @Test
    void testScheduledReportTask() throws IOException {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledTaskRunner.run(TASK_NAME_SCHEDULED_REPORT);

            File remoteDir = sftpServer.getRemoteDir();
            assertFileNamesInDir(remoteDir, DIR_TYPE_REMOTE, List.of("IT01.hdr", "IT01.det", "IT02.hdr", "IT02.det"));
            assertReportedToRtlDateNotNull(List.of(1L, 3L));
        }
    }

    @Test
    void testHousekeepingTask() {
        assertTrue(judgmentRepository.existsById(5L), "Judgment should exist before housekeeping");
        scheduledTaskRunner.run(TASK_NAME_HOUSEKEEPING);
        assertFalse(judgmentRepository.existsById(5L), "Judgment should not exist after housekeeping");
    }

    @Test
    void testUnknownTask() {
        Logger logger = (Logger) LoggerFactory.getLogger(ScheduledTaskRunner.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        scheduledTaskRunner.run(TASK_NAME_UNKNOWN);

        logger.detachAndStopAllAppenders();
        List<ILoggingEvent> logList = listAppender.list;

        int numLogMessages = logList.size();
        assertEquals(2, numLogMessages, "Unexpected number of messages in log");
        assertEquals("Task not found [" + TASK_NAME_UNKNOWN + "] (bean [" + BEAN_NAME_UNKNOWN + "])",
                     logList.get(numLogMessages - 1).getFormattedMessage(),
                     "Unexpected message in log");
    }

    private void assertReportedToRtlDateNotNull(List<Long> ids) {
        List<Judgment> judgments = judgmentRepository.findAllById(ids);

        for (Judgment judgment : judgments) {
            assertNotNull(judgment.getReportedToRtl(),
                          "Judgment with id [" + judgment.getId() + "] should not have a null reported to RTL date");
        }
    }
}
