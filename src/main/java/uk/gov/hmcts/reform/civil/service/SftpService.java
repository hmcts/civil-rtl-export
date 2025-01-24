package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;

@Service
@Slf4j
public class SftpService {

    @Value("${rtl-export.sftp.serverAddress}")
    String serverAddress;

    @Value("${rtl-export.sftp.serverPort}")
    String serverPort;

    @Value("${rtl-export.sftp.userName}")
    String userName;

    @Value("${rtl-export.sftp.password}")
    String password;

    @Value("${rtl-export.sftp.remoteDirectory}")
    String remoteDirectory;

    public boolean uploadFile(File judgmentFile) {
        try (StandardFileSystemManager manager = new StandardFileSystemManager()) {

            manager.init();

            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            SftpFileSystemConfigBuilder.getInstance().setSessionTimeout(opts, Duration.ofMillis(1000));

            String sftpUri = "sftp://" + userName + ":" + password + "@" + serverAddress + ":" + serverPort + "/"
                + remoteDirectory + "/" + judgmentFile.getName();

            FileObject localFile = manager.resolveFile(judgmentFile.getAbsolutePath());
            FileObject remoteFile = manager.resolveFile(sftpUri, opts);

            log.info("Starting file transfer to SFTP server...");
            remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);

            log.info("File {} successfully uploaded to SFTP server: {}", judgmentFile.getName(), serverAddress);

            return true;

        } catch (Exception ex) {
            log.error("Error during SFTP file transfer: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
