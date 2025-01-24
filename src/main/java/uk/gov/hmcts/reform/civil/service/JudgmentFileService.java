package uk.gov.hmcts.reform.civil.service;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.domain.Judgment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/*
First, the current date/time will be stored so that it can be used in the file generation process.

Each set of data will be iterated through and a file created for it.

The format of the file is described in the Scope for Initial Delivery.
Each line of the file is a fixed length, so values that are smaller than the total number of characters
for a particular field must be padded out.

The StringUtils class provided by the apache commons-lang3 dependency will be used to do this
via the leftPad() and rightPad() methods.

Each file will be saved in the standard temporary directory.

Each file will be named judgments-<timestamp>-<serviceId>.det.
If the asOf parameter was null then <timestamp> will be the stored current date/time, otherwise the value of asOf.
The format of <timestamp> will be YYYY-MM-DD-HH24-MI-SS-ss.

If the asOf parameter was null then each row of data will be updated with the stored current date/time.
The changes will then be saved to the database.

 */
@Service
public class JudgmentFileService {
    private static final Logger logger = LoggerFactory.getLogger(JudgmentFileService.class);

    private static final String TEMP_DIR = "judgment-files";

    private final SftpService sftpService;

    @Getter
    @Setter
    private File tmpDirectory;

    @Autowired
    public JudgmentFileService(SftpService sftpService) throws IOException {
        this.sftpService = sftpService;
        this.tmpDirectory = Files.createTempDirectory(TEMP_DIR).toFile();
    }

    //Main file service method
    //Creation, saving and sending of file for specified service ID and asOf timestamp

    public void createAndSendJudgmentFile(List<Judgment> judgments, LocalDateTime asOf,
                                          String serviceId, boolean test) {
        //checking if there are available judgments
        if (!judgments.isEmpty()) {

            //Generating data file content
            String dataFileContent = generateDataFileContent(judgments);
            File dataFile = saveToFile(dataFileContent, serviceId, asOf, "det");
            //Generating header file content
            String headerFileContent = generateHeaderFileContent(judgments.size(), asOf);
            File headerFile = saveToFile(headerFileContent, serviceId, asOf, "hdr");

            //Only transfer files to server via SFTP if not in test mode
            if (!test) {
                logger.info("Test mode is OFF. Transferring files to server via SFTP.");
                //Transferring files to server via SFTP
                boolean dataFileUpload = sftpService.uploadFile(dataFile);
                boolean headerFileUpload = sftpService.uploadFile(headerFile);

                if (dataFileUpload && headerFileUpload) {
                    // Update judgment report timestamp if successfully sent to BAIS
                    // Delete the files after successful upload
                    if (!dataFile.delete()) {
                        throw new IllegalStateException("Failed to delete data file: " + dataFile.getAbsolutePath());
                    }
                    if (!headerFile.delete()) {
                        throw new IllegalStateException("Failed to delete header file: "
                                                            + headerFile.getAbsolutePath());
                    }
                }
            } else {
                //Log that process is in test mode so no files will be sent to BAIS
                logger.info("Test mode is ON. Files will not be transferred to the server");
            }
        }
    }

    //File content generation method from judgments list
    private String generateDataFileContent(List<Judgment> judgments) {
        //StringBuilder is used to generate the toFormattedString content in the file
        StringBuilder sb = new StringBuilder();
        //For each judgment in judgment list, the formatted string is appended to StringBuilder
        //Each judgment appended with a newline ("\n") to separate the entries
        judgments.forEach(judgment -> sb.append(judgment.toFormattedString()).append("\n"));
        //Converts StringBuilder to a String containing separated formatted data that is returned
        return sb.toString();
    }
    /*
    The header file just needs to contain 1 line of data containing a count
    of the number of records in the data file and the date on which the file was created

    The number of records will be left justified, maximum of 10 characters padded out with spaces
    The date will be 8 characters in DDMMYYYY format

     */
    private String generateHeaderFileContent(int recordCount, LocalDateTime creationDate) {

        // If creationDate is null, use the current date and time
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }

        // Left-justified (right-padded) with spaces to 10 characters
        String recordCountString = StringUtils.rightPad(String.valueOf(recordCount), 10);

        // Date formatted as 8 characters in DDMMYYYY format
        String formattedDateString = creationDate.format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        // Record count and date string are joined together and returned
        return recordCountString + formattedDateString;
    }

    //Method for data file creation, formatting and writing to file
    private File saveToFile(String content, String serviceId, LocalDateTime timestamp, String fileExtension) {
        // Define the date-time format pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        // If creationDate is null, use the current date and time
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        // Format the timestamp to match the expected filename
        String formattedDate = timestamp.format(formatter);

        // Generate the file name
        String fileName = String.format("judgment-%s-%s.%s", formattedDate, serviceId, fileExtension);
        File file = new File(tmpDirectory, fileName);
        logger.info("Saving file {}", file.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            // Log the exception
            logger.error("Error while saving the file: ", e);
        }

        return file;
    }
}
