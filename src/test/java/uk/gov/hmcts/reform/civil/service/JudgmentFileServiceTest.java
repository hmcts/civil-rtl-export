package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JudgmentFileServiceTest {
    private JudgmentFileService judgmentFileService;
    @Mock
    private SftpService sftpService;
    @Mock
    private JudgmentRepository judgmentRepository;

    @BeforeEach
    void setUp() throws IOException {
        judgmentFileService = new JudgmentFileService(sftpService);
    }

    @Test
    void shouldHandleNoJudgments() {
        String serviceId = "service1";
        LocalDateTime asOf = null;
        List<Judgment> judgments = Collections.emptyList();
        boolean test = false;

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, serviceId, test);

        verify(sftpService, never()).uploadFile(any(File.class));
    }

    @Test
    void shouldCreateAndSendFilesForValidJudgments() {
        // Service ID, asOf value and mock judgments set
        final String serviceId = "service1";
        final LocalDateTime asOf = null;
        final boolean test = false;
        Judgment judgment1 = new Judgment();
        Judgment judgment2 = new Judgment();

        // Call admin order date method on judgments
        judgment1.setJudgmentAdminOrderDate(LocalDate.now());
        judgment2.setJudgmentAdminOrderDate(LocalDate.now());

        // Mocks the list of the two judgments returned when the file is created and sent
        List<Judgment> judgments = List.of(judgment1, judgment2);

        // Mocks SFTP service to return true if any file is successfully uploaded
        when(sftpService.uploadFile(any(File.class))).thenReturn(true);

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, serviceId, test);

        // Verifying that SFTP service uploaded the 2 judgment files
        verify(sftpService, times(2)).uploadFile(any(File.class));
    }

    @Test
    void shouldDeleteFilesAfterUpload() throws IOException {
        final String serviceId = "service1";
        final LocalDateTime asOf = LocalDateTime.now();
        // Test mode set to false to trigger the file upload and deletion
        final boolean test = false;

        Judgment judgment1 = new Judgment();
        Judgment judgment2 = new Judgment();

        judgment1.setJudgmentAdminOrderDate(LocalDate.now());
        judgment2.setJudgmentAdminOrderDate(LocalDate.now());

        // Mocks the list of the two judgments returned when the file is created and sent
        List<Judgment> judgments = List.of(judgment1, judgment2);

        // Temporary directory for where files will be saved
        File tmpDirFile = judgmentFileService.getTmpDirectory();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = asOf.format(formatter);

        File detailsFile = new File(tmpDirFile, String.format("judgment-%s-%s.%s", formattedDate, serviceId, "det"));
        File headerFile = new File(tmpDirFile, String.format("judgment-%s-%s.%s", formattedDate, serviceId, "hdr"));

        when(sftpService.uploadFile(detailsFile)).thenReturn(true);
        when(sftpService.uploadFile(headerFile)).thenReturn(true);

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, serviceId, test);

        // Asserting the temporary file is deleted
        assertFalse(detailsFile.exists(), "The data file should have been deleted after upload");
        assertFalse(headerFile.exists(), "The header file should have been deleted after upload");

        tmpDirFile.deleteOnExit();
    }


    @Test
    void shouldFormatJudgmentsCorrectlyInDataFile() throws IOException {
        // Mock creation of two judgments
        Judgment judgment1 = mock(Judgment.class);
        Judgment judgment2 = mock(Judgment.class);

        // Formatting the strings of the mock judgments
        when(judgment1.toFormattedString()).thenReturn("JUDGMENT_1");
        when(judgment2.toFormattedString()).thenReturn("JUDGMENT_2");

        LocalDateTime asOf = LocalDateTime.now();
        String serviceId = "service1";

        // Mock generation of the data file content using the list of both judgments
        judgmentFileService.createAndSendJudgmentFile(List.of(judgment1, judgment2), asOf, serviceId, true);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = asOf.format(formatter);
        String detailsFileName = String.format("judgment-%s-%s.%s", formattedDate, serviceId, "det");
        String headerFileName = String.format("judgment-%s-%s.%s", formattedDate, serviceId, "hdr");

        // Temporary directory for where files will be saved
        File tmpDirFile = judgmentFileService.getTmpDirectory();

        Path detailsFilePath = Paths.get(tmpDirFile.getAbsolutePath(), detailsFileName);
        Path headerFilePath = Paths.get(tmpDirFile.getAbsolutePath(), headerFileName);

        try {
            List<String> result = Files.readAllLines(detailsFilePath);
            assertEquals("JUDGMENT_1", result.get(0), "Data file line 1 is incorrectly formatted");
            assertEquals("JUDGMENT_2", result.get(1), "Data file line 2 is incorrectly formatted");

            String headerContent = Files.readString(headerFilePath);

            String expectedHeaderContent = String.format(
                "%-10s%s",
                2,
                asOf.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
            );
            assertEquals(expectedHeaderContent, headerContent, "Header file content is incorrectly formatted");

        } finally {
            Files.delete(detailsFilePath);
            Files.delete(headerFilePath);
        }

    }

    @Test
    void shouldNotUploadFilesInTestMode() {
        String serviceId = "service1";
        LocalDateTime asOf = LocalDateTime.now();
        boolean test = true;

        Judgment judgment = new Judgment();

        judgment.setJudgmentAdminOrderDate(LocalDate.now());

        List<Judgment> judgments = List.of(judgment);

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, serviceId, test);

        // Verify that no files are uploaded in test mode
        verify(sftpService, never()).uploadFile(any(File.class));

        // Verify that files are still created but not uploaded
        File tmpDirFile = judgmentFileService.getTmpDirectory();
        String formattedDate = asOf.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
        File detailsFile = new File(tmpDirFile, String.format("judgment-%s-%s.det", formattedDate, serviceId));
        File headerFile = new File(tmpDirFile, String.format("judgment-%s-%s.hdr", formattedDate, serviceId));

        assertTrue(detailsFile.exists(), "Details file should still be created in test mode");
        assertTrue(headerFile.exists(), "Header file should still be created in test mode");

    }


}
