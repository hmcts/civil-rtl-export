package uk.gov.hmcts.reform.civil.service.sftp;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.exception.SftpException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SftpServiceTest {

    private static final String SFTP_HOST = "sftp_host";
    private static final int SFTP_PORT = 22;
    private static final String SFTP_USERNAME = "sftp_user";
    private static final String SFTP_PASSWORD = "sftp_pass";
    private static final String SFTP_REMOTE_DIR = "sftp_remote_dir";
    private static final int SFTP_CONNECT_TIMEOUT = 5000;
    private static final int SFTP_AUTH_TIMEOUT = 5000;

    private static final String FILE_NAME = "file.txt";

    private static final String CAUSE_MESSAGE = "sftp exception message";
    private static final String ERROR_SFTP_EX_NOT_THROWN = "SftpException should be thrown";

    private SftpService sftpService;

    @ParameterizedTest
    @MethodSource("testUploadFilesParams")
    void testUploadFiles(String remoteDir, String expectedRemotePath) throws IOException {
        sftpService = new SftpService(createSftpConfigProperties(remoteDir));

        List<File> files = new ArrayList<>();

        File mockFile = mock(File.class);
        Path mockFilePath = mock(Path.class);
        when(mockFile.toPath()).thenReturn(mockFilePath);
        when(mockFile.getName()).thenReturn(FILE_NAME);
        files.add(mockFile);

        try (MockedStatic<SshClient> mockStaticSshClient = mockStatic(SshClient.class);
             MockedStatic<SftpClientFactory> mockStaticSftpClientFactory = mockStatic(SftpClientFactory.class)) {

            ClientSession mockClientSession = mock(ClientSession.class);

            AuthFuture mockAuthFuture = mock(AuthFuture.class);
            when(mockClientSession.auth()).thenReturn(mockAuthFuture);

            ConnectFuture mockConnectFuture = mock(ConnectFuture.class);
            when(mockConnectFuture.verify(SFTP_CONNECT_TIMEOUT)).thenReturn(mockConnectFuture);
            when(mockConnectFuture.getClientSession()).thenReturn(mockClientSession);

            SshClient mockSshClient = mock(SshClient.class);
            when(mockSshClient.connect(SFTP_USERNAME, SFTP_HOST, SFTP_PORT)).thenReturn(mockConnectFuture);

            mockStaticSshClient.when(SshClient::setUpDefaultClient).thenReturn(mockSshClient);

            SftpClientFactory mockSftpClientFactory = mock(SftpClientFactory.class);

            SftpClient mockSftpClient = mock(SftpClient.class);
            when(mockSftpClientFactory.createSftpClient(mockClientSession)).thenReturn(mockSftpClient);

            mockStaticSftpClientFactory.when(SftpClientFactory::instance).thenReturn(mockSftpClientFactory);

            sftpService.uploadFiles(files);

            verify(mockSshClient).start();
            verify(mockSshClient).connect(SFTP_USERNAME, SFTP_HOST, SFTP_PORT);

            verify(mockConnectFuture).verify(SFTP_CONNECT_TIMEOUT);
            verify(mockConnectFuture).getClientSession();

            verify(mockClientSession).addPasswordIdentity(SFTP_PASSWORD);
            verify(mockClientSession).auth();

            verify(mockAuthFuture).verify(SFTP_AUTH_TIMEOUT);

            verify(mockSftpClient).put(mockFilePath, expectedRemotePath);
        }
    }

    @Test
    void testUploadFilesSftpExceptionSshClient() throws IOException {
        sftpService = new SftpService(createSftpConfigProperties(SFTP_REMOTE_DIR));

        List<File> files = new ArrayList<>();

        File mockFile = mock(File.class);
        files.add(mockFile);

        try (MockedStatic<SshClient> mockStaticSshClient = mockStatic(SshClient.class)) {

            IOException ioException = new IOException(CAUSE_MESSAGE);

            SshClient mockSshClient = mock(SshClient.class);
            when(mockSshClient.connect(SFTP_USERNAME, SFTP_HOST, SFTP_PORT)).thenThrow(ioException);

            mockStaticSshClient.when(SshClient::setUpDefaultClient).thenReturn(mockSshClient);

            SftpException sftpException =
                assertThrows(SftpException.class, () -> sftpService.uploadFiles(files), ERROR_SFTP_EX_NOT_THROWN);
            assertSftpException(sftpException);

            verify(mockSshClient).start();
            verify(mockSshClient).connect(SFTP_USERNAME, SFTP_HOST, SFTP_PORT);
        }
    }

    @Test
    void testUploadFilesSftpExceptionSftpClient() throws IOException {
        sftpService = new SftpService(createSftpConfigProperties(SFTP_REMOTE_DIR));

        List<File> files = new ArrayList<>();

        File mockFile = mock(File.class);
        files.add(mockFile);

        try (MockedStatic<SshClient> mockStaticSshClient = mockStatic(SshClient.class);
             MockedStatic<SftpClientFactory> mockStaticSftpClientFactory = mockStatic(SftpClientFactory.class)) {

            ClientSession mockClientSession = mock(ClientSession.class);

            AuthFuture mockAuthFuture = mock(AuthFuture.class);
            when(mockClientSession.auth()).thenReturn(mockAuthFuture);

            ConnectFuture mockConnectFuture = mock(ConnectFuture.class);
            when(mockConnectFuture.verify(SFTP_CONNECT_TIMEOUT)).thenReturn(mockConnectFuture);
            when(mockConnectFuture.getClientSession()).thenReturn(mockClientSession);

            SshClient mockSshClient = mock(SshClient.class);
            when(mockSshClient.connect(SFTP_USERNAME, SFTP_HOST, SFTP_PORT)).thenReturn(mockConnectFuture);

            mockStaticSshClient.when(SshClient::setUpDefaultClient).thenReturn(mockSshClient);

            SftpClientFactory mockSftpClientFactory = mock(SftpClientFactory.class);

            IOException ioException = new IOException(CAUSE_MESSAGE);
            when(mockSftpClientFactory.createSftpClient(mockClientSession)).thenThrow(ioException);

            mockStaticSftpClientFactory.when(SftpClientFactory::instance).thenReturn(mockSftpClientFactory);

            SftpException sftpException =
                assertThrows(SftpException.class, () -> sftpService.uploadFiles(files), ERROR_SFTP_EX_NOT_THROWN);
            assertSftpException(sftpException);

            verify(mockSshClient).start();
            verify(mockSshClient).connect(SFTP_USERNAME, SFTP_HOST, SFTP_PORT);

            verify(mockConnectFuture).verify(SFTP_CONNECT_TIMEOUT);
            verify(mockConnectFuture).getClientSession();

            verify(mockClientSession).addPasswordIdentity(SFTP_PASSWORD);
            verify(mockClientSession).auth();

            verify(mockAuthFuture).verify(SFTP_AUTH_TIMEOUT);
        }
    }

    private SftpConfigProperties createSftpConfigProperties(String remoteDir) {
        SftpConfigProperties configProperties = new SftpConfigProperties();

        configProperties.setHost(SFTP_HOST);
        configProperties.setServerPort(SFTP_PORT);
        configProperties.setUserName(SFTP_USERNAME);
        configProperties.setPassword(SFTP_PASSWORD);
        configProperties.setRemoteDir(remoteDir);
        configProperties.setConnectTimeout(SFTP_CONNECT_TIMEOUT);
        configProperties.setAuthTimeout(SFTP_AUTH_TIMEOUT);

        return configProperties;
    }

    private static Stream<Arguments> testUploadFilesParams() {
        return Stream.of(
            arguments(null, FILE_NAME),
            arguments("", FILE_NAME),
            arguments(" ", FILE_NAME),
            arguments(SFTP_REMOTE_DIR, SFTP_REMOTE_DIR + "/" + FILE_NAME)
        );
    }

    private void assertSftpException(SftpException sftpException) {
        Throwable cause = sftpException.getCause();
        assertNotNull(cause, "SftpException should have a cause");
        assertEquals(CAUSE_MESSAGE, cause.getMessage(), "SftpException cause does not have expected message");
    }
}
