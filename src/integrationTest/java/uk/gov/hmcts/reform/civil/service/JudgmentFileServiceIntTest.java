package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.service.sftp.SftpConfigProperties;
import uk.gov.hmcts.reform.civil.service.sftp.SftpService;
import uk.gov.hmcts.reform.civil.util.LocalSftpServer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.DIR_TYPE_REMOTE;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.DIR_TYPE_TEMP;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertFileInDir;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertNoFilesInDir;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertNumFilesInDir;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    initializers = ConfigDataApplicationContextInitializer.class,
    classes = {JudgmentFileService.class, SftpService.class}
)
@EnableConfigurationProperties(SftpConfigProperties.class)
@ActiveProfiles("itest")
class JudgmentFileServiceIntTest {

    private static final LocalDateTime DATE_TIME_AS_OF = LocalDateTime.of(2025, 1, 1, 15, 23, 10);
    private static final String SERVICE_ID_1 = "IT01";

    private static final String EXPECTED_HEADER_FILE_CONTENT = "1         01012025";

    private static final String FILE_BASE_NAME = "judgment-2025-01-01-15-23-10-IT01";
    private static final String FILE_EXTENSION_DETAILS = ".det";
    private static final String FILE_EXTENSION_HEADER = ".hdr";

    private final int sftpPort;
    private final String sftpRemoteDir;

    @TempDir
    private File sftpRootDir;

    private final JudgmentFileService judgmentFileService;

    @Autowired
    public JudgmentFileServiceIntTest(JudgmentFileService judgmentFileService,
                                      @Value("${rtl-export.sftp.serverPort}") int sftpPort,
                                      @Value("${rtl-export.sftp.remoteDir}") String sftpRemoteDir) {
        this.judgmentFileService = judgmentFileService;
        this.sftpPort = sftpPort;
        this.sftpRemoteDir = sftpRemoteDir;
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testCreateAndSendNoJudgments(boolean testMode) throws IOException {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            judgmentFileService.createAndSendJudgmentFile(Collections.emptyList(), null, SERVICE_ID_1, testMode);

            assertNoFilesInDir(judgmentFileService.getTmpDirectory(), DIR_TYPE_TEMP);
            assertNoFilesInDir(sftpServer.getRemoteDir(), DIR_TYPE_REMOTE);
        }
    }

    @Test
    void testCreateAndSendJudgments() throws IOException {
        List<Judgment> judgments = new ArrayList<>();
        judgments.add(createJudgment());

        String headerFileName = FILE_BASE_NAME + FILE_EXTENSION_HEADER;
        String detailsFileName = FILE_BASE_NAME + FILE_EXTENSION_DETAILS;

        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            judgmentFileService.createAndSendJudgmentFile(judgments, DATE_TIME_AS_OF, SERVICE_ID_1, false);

            File tempDir = judgmentFileService.getTmpDirectory();
            assertNoFilesInDir(tempDir, DIR_TYPE_TEMP);

            File remoteDir = sftpServer.getRemoteDir();
            assertNumFilesInDir(remoteDir, DIR_TYPE_REMOTE, 2);
            assertFileInDir(remoteDir, DIR_TYPE_REMOTE, headerFileName, List.of(EXPECTED_HEADER_FILE_CONTENT));
            assertFileInDir(remoteDir, DIR_TYPE_REMOTE, detailsFileName, List.of(getExpectedDetailsFileContent()));
        }
    }

    @Test
    void testCreateAndSendJudgmentsTestMode() throws IOException {
        List<Judgment> judgments = new ArrayList<>();
        judgments.add(createJudgment());

        String headerFileName = FILE_BASE_NAME + FILE_EXTENSION_HEADER;
        String detailsFileName = FILE_BASE_NAME + FILE_EXTENSION_DETAILS;

        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            judgmentFileService.createAndSendJudgmentFile(judgments, DATE_TIME_AS_OF, SERVICE_ID_1, true);

            File tempDir = judgmentFileService.getTmpDirectory();
            assertNumFilesInDir(tempDir, DIR_TYPE_TEMP, 2);
            assertFileInDir(tempDir, DIR_TYPE_TEMP, headerFileName, List.of(EXPECTED_HEADER_FILE_CONTENT));
            assertFileInDir(tempDir, DIR_TYPE_TEMP, detailsFileName, List.of(getExpectedDetailsFileContent()));

            File remoteDir = sftpServer.getRemoteDir();
            assertNoFilesInDir(remoteDir, DIR_TYPE_REMOTE);
        }
    }

    private Judgment createJudgment() {
        Judgment judgment = new Judgment();

        judgment.setCourtCode("111");
        judgment.setCaseNumber("CASENO11");
        judgment.setJudgmentAdminOrderTotal(new BigDecimal("11.11"));
        judgment.setJudgmentAdminOrderDate(LocalDate.of(2025, 1, 1));
        judgment.setRegistrationType("R");
        judgment.setDefendantName("DEFENDANT1");
        judgment.setDefendantAddressLine1("DEFENDANT1 LINE1");
        judgment.setDefendantAddressPostcode("DD1 1DD");

        return judgment;
    }

    private String getExpectedDetailsFileContent() {
        return "111CASENO1100000011.1101012025R        "
            + "DEFENDANT1                                                            "
            + "DEFENDANT1 LINE1                   "
            + "                                   "
            + "                                   "
            + "                                   "
            + "                                   "
            + "DD1 1DD "
            + "        ";
    }
}
