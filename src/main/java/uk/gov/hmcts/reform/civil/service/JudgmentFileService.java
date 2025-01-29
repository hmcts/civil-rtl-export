package uk.gov.hmcts.reform.civil.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;
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

    private static final String TEMP_DIR = "judgment-files";

    private static final String FILE_EXTENSION_HEADER = "hdr";
    private static final String FILE_EXTENSION_DETAILS = "det";

    private final SftpService sftpService;

    @Getter
    @Setter
    private File tmpDirectory;

    @Autowired
    public JudgmentFileService(SftpService sftpService) throws IOException {
        this.sftpService = sftpService;
        this.tmpDirectory = Files.createTempDirectory(TEMP_DIR).toFile();
    }

    public void createAndSendJudgmentFile(List<Judgment> judgments, LocalDateTime asOf,
                                          String serviceId, boolean test) {
        if (!judgments.isEmpty()) {

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

                if (!dataFile.delete()) {
                    throw new IllegalStateException("Failed to delete data file: " + dataFile.getAbsolutePath());
                }
                if (!headerFile.delete()) {
                    throw new IllegalStateException("Failed to delete header file: " + headerFile.getAbsolutePath());
                }
            } else {
                log.info("Test mode is ON. Files will not be transferred to the server");
            }
        }
    }

    private String generateDataFileContent(List<Judgment> judgments) {
        StringBuilder sb = new StringBuilder();
        judgments.forEach(judgment -> sb.append(judgment.toFormattedString()).append("\n"));

        return sb.toString();
    }

    private String generateHeaderFileContent(int recordCount, LocalDateTime creationDate) {
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }

        String recordCountString = StringUtils.rightPad(String.valueOf(recordCount), 10);
        String formattedDateString = creationDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        return recordCountString + formattedDateString;
    }

    private File saveToFile(String content, String serviceId, LocalDateTime timestamp, String fileExtension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        String formattedDate = timestamp.format(formatter);

        String fileName = String.format("judgment-%s-%s.%s", formattedDate, serviceId, fileExtension);
        File file = new File(tmpDirectory, fileName);
        log.info("Saving file {}", file.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            log.error("Error while saving the file: ", e);
        }

        return file;
    }
}
