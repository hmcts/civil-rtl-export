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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.DIR_TYPE_REMOTE;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertFileNamesInDir;

@SpringBootTest
@ActiveProfiles("itest")
@Transactional
@Sql("scheduled_report_task_int_test.sql")
class ScheduledReportTaskIntTest {

    private final JudgmentRepository judgmentRepository;

    private final ScheduledReportTask scheduledReportTask;

    private final int sftpPort;
    private final String sftpRemoteDir;

    @TempDir
    private File sftpRootDir;

    @Autowired
    public ScheduledReportTaskIntTest(ScheduledReportTask scheduledReportTask,
                                      JudgmentRepository judgmentRepository,
                                      @Value("${rtl-export.sftp.serverPort}") int sftpPort,
                                      @Value("${rtl-export.sftp.remoteDir}") String sftpRemoteDir) {
        this.scheduledReportTask = scheduledReportTask;
        this.judgmentRepository = judgmentRepository;
        this.sftpPort = sftpPort;
        this.sftpRemoteDir = sftpRemoteDir;
    }

    @Test
    void testScheduledReportTask() throws IOException {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledReportTask.run();

            File remoteDir = sftpServer.getRemoteDir();
            assertFileNamesInDir(remoteDir, DIR_TYPE_REMOTE, List.of("IT01.hdr", "IT01.det", "IT02.hdr", "IT02.det"));
            assertReportedToRtlDateNotNull(List.of(1L, 3L));
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
