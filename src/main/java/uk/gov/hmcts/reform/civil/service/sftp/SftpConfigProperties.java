package uk.gov.hmcts.reform.civil.service.sftp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rtl-export.sftp")
@Getter
@Setter
public class SftpConfigProperties {

    private String host;
    private int serverPort;
    private String userName;
    private String password;
    private String remoteDir;
    private int connectTimeout;
    private int authTimeout;
}
