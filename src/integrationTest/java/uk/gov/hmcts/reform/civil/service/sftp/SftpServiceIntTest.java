package uk.gov.hmcts.reform.civil.service.sftp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.exception.SftpException;
import uk.gov.hmcts.reform.civil.util.LocalSftpServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.DIR_TYPE_REMOTE;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertFileNamesInDir;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertNoFilesInDir;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    initializers = ConfigDataApplicationContextInitializer.class,
    classes = {SftpService.class}
)
@EnableConfigurationProperties(SftpConfigProperties.class)
@ActiveProfiles("itest")
class SftpServiceIntTest {

    private static final String TEST_FILE_DIR = "src/integrationTest/resources/uk/gov/hmcts/reform/civil/service/sftp/";
    private static final String TEST_FILE_NAME = "sftp_test_file.txt";

    private final int sftpPort;
    private final String sftpRemoteDir;

    @TempDir
    private File sftpRootDir;

    private final SftpService sftpService;

    @Autowired
    public SftpServiceIntTest(SftpService sftpService,
                              @Value("${rtl-export.sftp.serverPort}") int sftpPort,
                              @Value("${rtl-export.sftp.remoteDir}") String sftpRemoteDir) {
        this.sftpService = sftpService;
        this.sftpPort = sftpPort;
        this.sftpRemoteDir = sftpRemoteDir;
    }

    @Test
    void testUploadFiles() throws IOException {

        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            List<File> dataFiles = new ArrayList<>();
            File testFile = new File(TEST_FILE_DIR + TEST_FILE_NAME);
            dataFiles.add(testFile);

            sftpService.uploadFiles(dataFiles);

            File remoteDir = sftpServer.getRemoteDir();
            assertFileNamesInDir(remoteDir, DIR_TYPE_REMOTE, List.of(TEST_FILE_NAME));
        }
    }

    @Test
    void testUploadFilesSftpException() throws IOException {

        // Create an SFTP server on a different port to simulate a connection failure
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort + 1, sftpRootDir, sftpRemoteDir)) {
            List<File> dataFiles = new ArrayList<>();
            File testFile = new File(TEST_FILE_DIR + TEST_FILE_NAME);
            dataFiles.add(testFile);

            SftpException exception =
                assertThrows(SftpException.class,
                             () -> sftpService.uploadFiles(dataFiles),
                             "Expected SftpException not thrown");
            assertTrue(exception.getMessage().contains("Connection refused"),
                       "Exception message does not contain 'Connection refused'");

            File remoteDir = sftpServer.getRemoteDir();
            assertNoFilesInDir(remoteDir, DIR_TYPE_REMOTE);
        }
    }
}
