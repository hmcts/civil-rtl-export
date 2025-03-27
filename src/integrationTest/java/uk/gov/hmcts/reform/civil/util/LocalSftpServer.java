package uk.gov.hmcts.reform.civil.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Slf4j
public class LocalSftpServer implements AutoCloseable {

    private static final String PATH_DELIMITER = "/";

    private final SshServer sshServer;

    @Getter
    private final File remoteDir;

    private LocalSftpServer(int port, File rootDir, String remoteDirName) throws IOException {

        if (remoteDirName != null && !remoteDirName.isBlank()) {
            remoteDir = new File(rootDir, PATH_DELIMITER + remoteDirName);
            boolean remoteDirCreated = remoteDir.mkdir();
            if (!remoteDirCreated) {
                throw new IOException("Unable to create remote directory ["
                                          + rootDir.getAbsolutePath() + PATH_DELIMITER + remoteDirName + "]");
            }
        } else {
            remoteDir = rootDir;
        }
        log.debug("Remote dir name: [{}], Remote dir path: [{}]", remoteDirName, remoteDir.getAbsolutePath());

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);

        VirtualFileSystemFactory virtualFileSystemFactory = new VirtualFileSystemFactory(rootDir.toPath());
        sshServer.setFileSystemFactory(virtualFileSystemFactory);

        SftpSubsystemFactory sftpFactory = new SftpSubsystemFactory.Builder().build();
        sshServer.setSubsystemFactories(Collections.singletonList(sftpFactory));

        // Permit any public SSH key
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshServer.setPublickeyAuthenticator((username, key, session) -> true);

        // Permit any user
        sshServer.setPasswordAuthenticator((username, password, session) -> true);

        sshServer.start();

        log.debug("Local SFTP server started");
    }

    public static LocalSftpServer create(int port, File rootDir, String remoteDirName) throws IOException {
        return new LocalSftpServer(port, rootDir, remoteDirName);
    }

    @Override
    public void close() throws IOException {
        sshServer.stop();
        log.debug("Local SFTP server stopped");
    }
}
