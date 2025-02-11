package uk.gov.hmcts.reform.civil.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.exception.DeleteFileException;
import uk.gov.hmcts.reform.civil.exception.SaveFileException;
import uk.gov.hmcts.reform.civil.service.sftp.SftpService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JudgmentFileService {

    private static final String TEMP_DIR_PREFIX = "judgment-files";

    private static final String FILE_EXTENSION_HEADER = "hdr";
    private static final String FILE_EXTENSION_DETAILS = "det";

    private static final DateTimeFormatter DATE_FORMAT_HEADER_CREATION_DATE = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DATE_FORMAT_FILE_NAME = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private final SftpService sftpService;

    @Getter
    private final File tmpDirectory;

    @Autowired
    public JudgmentFileService(SftpService sftpService) throws IOException {
        this.sftpService = sftpService;
        this.tmpDirectory = Files.createTempDirectory(TEMP_DIR_PREFIX).toFile();
    }

    public void createAndSendJudgmentFile(List<Judgment> judgments,
                                          LocalDateTime asOf,
                                          String serviceId,
                                          boolean test) {
        if (!judgments.isEmpty()) {
            log.info("Generating and saving files for serviceId [{}]", serviceId);

            String dataFileContent = generateDataFileContent(judgments);
            File dataFile = saveToFile(dataFileContent, serviceId, asOf, FILE_EXTENSION_DETAILS);

            String headerFileContent = generateHeaderFileContent(judgments.size(), asOf);
            File headerFile = saveToFile(headerFileContent, serviceId, asOf, FILE_EXTENSION_HEADER);

            if (!test) {
                log.info("Test mode is OFF. Transferring files to server via SFTP.");

                List<File> dataFiles = new ArrayList<>();
                dataFiles.add(dataFile);
                dataFiles.add(headerFile);

                sftpService.uploadFiles(dataFiles);

                for (File file : dataFiles) {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException e) {
                        throw new DeleteFileException("Unable to delete file [" + file.getName() + "]");
                    }
                }
            } else {
                log.info("Test mode is ON. Files will not be transferred to the server");
            }
        }
    }

    private String generateDataFileContent(List<Judgment> judgments) {
        log.debug("Generating data file content");
        StringBuilder sb = new StringBuilder();
        judgments.forEach(judgment -> sb.append(judgment.toFormattedString()).append("\n"));

        return sb.toString();
    }

    private String generateHeaderFileContent(int recordCount, LocalDateTime creationDate) {
        log.debug("Generating header file content");
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }

        String recordCountString = StringUtils.rightPad(String.valueOf(recordCount), 10);
        String formattedDateString = creationDate.format(DATE_FORMAT_HEADER_CREATION_DATE);

        return recordCountString + formattedDateString;
    }

    private File saveToFile(String content, String serviceId, LocalDateTime timestamp, String fileExtension) {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        String formattedDate = timestamp.format(DATE_FORMAT_FILE_NAME);

        String fileName = String.format("judgment-%s-%s.%s", formattedDate, serviceId, fileExtension);
        File file = new File(tmpDirectory, fileName);
        log.debug("Saving file [{}]", file.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            throw new SaveFileException(e);
        }

        return file;
    }
}
