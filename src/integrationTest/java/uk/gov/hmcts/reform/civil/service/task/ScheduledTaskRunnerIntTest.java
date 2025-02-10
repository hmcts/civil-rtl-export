package uk.gov.hmcts.reform.civil.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("itest")
@Transactional
@Sql("scheduled_task_runner_int_test.sql")
class ScheduledTaskRunnerIntTest {

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
    void testTaskScheduledReport() throws IOException {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledTaskRunner.run(ScheduledTaskRunner.TASK_NAME_SCHEDULED_REPORT);

            File remoteDir = sftpServer.getRemoteDir();
            assertFilesInRemoteDir(remoteDir, List.of("IT01.hdr", "IT01.det", "IT02.hdr", "IT02.det"));
            assertReportedToRtlDateNotNull(List.of(1L, 3L));
        }
    }

    @Test
    void testTaskHousekeeping() {
        assertTrue(judgmentRepository.existsById(5L), "Judgment should exist before housekeeping");
        scheduledTaskRunner.run(ScheduledTaskRunner.TASK_NAME_HOUSEKEEPING);
        assertFalse(judgmentRepository.existsById(5L), "Judgment should not exist after housekeeping");
    }

    private void assertFilesInRemoteDir(File remoteDir, List<String> fileNames) {
        String[] filesInDir = remoteDir.list();
        assertNotNull(filesInDir, "Remote directory listing should not be null");

        assertEquals(fileNames.size(), filesInDir.length, "Remote directory contains unexpected number of files");

        List<String> remoteFiles = Arrays.asList(filesInDir);
        for (String fileName : fileNames) {
            assertTrue(remoteFiles.stream().anyMatch(name -> name.endsWith(fileName)),
                       "Remote directory should contain file " + fileName);
        }
    }

    private void assertReportedToRtlDateNotNull(List<Long> ids) {
        List<Judgment> judgments = judgmentRepository.findAllById(ids);

        for (Judgment judgment : judgments) {
            assertNotNull(judgment.getReportedToRtl(),
                          "Judgment with id [" + judgment.getId() + "] should not have a null reported to RTL date");
        }
    }
}
