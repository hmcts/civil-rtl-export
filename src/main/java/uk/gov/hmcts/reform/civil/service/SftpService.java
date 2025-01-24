package uk.gov.hmcts.reform.civil.service;


import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;

/*

Send files to BAIS
After the files have been created they will be sent via SFTP to BAIS.
The connection details will be retrieved from the corresponding key vault secrets.

Once a file has been sent it will be deleted.

Apache Commons route
 */
@Service
public class SftpService {
    private static final Logger logger = LoggerFactory.getLogger(SftpService.class);


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


            // Initialises file manager
            manager.init();

            //Set up SFTP configuration
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
            SftpFileSystemConfigBuilder.getInstance().setSessionTimeout(opts, Duration.ofMillis(1000));


            //Create the SFTP URI using host name, userName, password, remote path and file name
            String sftpUri = "sftp://" + userName + ":" + password + "@" + serverAddress + ":" + serverPort + "/"
                + remoteDirectory + "/" + judgmentFile.getName();

            //Create local and remote file objects
            FileObject localFile = manager.resolveFile(judgmentFile.getAbsolutePath());
            FileObject remoteFile = manager.resolveFile(sftpUri, opts);

            //Copy local file to sftp server
            logger.info("Starting file transfer to SFTP server...");
            remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
            logger.info("File {} successfully uploaded to SFTP server: {}", judgmentFile.getName(), serverAddress);

            return true;

        } catch (Exception ex) {
            logger.error("Error during SFTP file transfer: {}", ex.getMessage(), ex);
            return false;
        }
    }




}
