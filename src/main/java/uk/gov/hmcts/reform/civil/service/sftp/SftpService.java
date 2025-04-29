package uk.gov.hmcts.reform.civil.service.sftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exception.SftpException;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class SftpService {

    private static final String PATH_DELIMITER = "/";

    private final SftpConfigProperties sftpConfig;

    @Autowired
    public SftpService(SftpConfigProperties sftpConfig) {
        this.sftpConfig = sftpConfig;
    }

    public void uploadFiles(List<File> dataFiles) {

        log.debug("Creating SshClient");
        try (SshClient sshClient = SshClient.setUpDefaultClient()) {

            log.debug("Starting SshClient");
            sshClient.start();

            log.debug("Creating client session to SFTP server. Host: [{}], Port: [{}], Username: [{}]",
                      sftpConfig.getHost(),
                      sftpConfig.getServerPort(),
                      sftpConfig.getUserName());
            try (ClientSession session =
                     sshClient.connect(sftpConfig.getUserName(), sftpConfig.getHost(), sftpConfig.getServerPort())
                         .verify(sftpConfig.getConnectTimeout())
                         .getClientSession()) {

                log.debug("Authenticating with SFTP server");
                session.addPasswordIdentity(sftpConfig.getPassword());
                session.auth().verify(sftpConfig.getAuthTimeout());

                try (SftpClient sftpClient = SftpClientFactory.instance().createSftpClient(session)) {

                    String remotePathPrefix = getRemotePathPrefix();

                    for (File dataFile : dataFiles) {
                        String remotePath = remotePathPrefix + dataFile.getName();

                        log.info("Uploading file [{}] to [{}] on SFTP server", dataFile.getName(), remotePath);
                        sftpClient.put(dataFile.toPath(), remotePath);
                        log.info("File [{}] uploaded to [{}] on SFTP server", dataFile.getName(), remotePath);
                    }
                }
            }

        } catch (IOException e) {
            throw new SftpException(e);
        }
    }

    private String getRemotePathPrefix() {
        String remoteDir = sftpConfig.getRemoteDir();
        return (remoteDir != null && !remoteDir.isBlank()) ? remoteDir + PATH_DELIMITER : "";
    }
}
