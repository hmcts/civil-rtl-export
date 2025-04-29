package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.exception.DeleteFileException;
import uk.gov.hmcts.reform.civil.service.sftp.SftpService;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudgmentFileServiceTest {

    private static final String SERVICE_ID_1 = "UT01";

    private static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    private static final String FILE_NAME_EXTENSION_HEADER = "hdr";
    private static final String FILE_NAME_EXTENSION_DETAILS = "det";

    private JudgmentFileService judgmentFileService;

    @Mock
    private SftpService mockSftpService;

    @BeforeEach
    void setUp() throws IOException {
        judgmentFileService = new JudgmentFileService(mockSftpService);
    }

    @Test
    void shouldHandleNoJudgments() {
        LocalDateTime asOf = null;
        List<Judgment> judgments = Collections.emptyList();
        boolean test = false;

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, SERVICE_ID_1, test);

        verify(mockSftpService, never()).uploadFiles(anyList());
    }

    @Test
    void shouldCreateAndSendFilesForValidJudgments() {
        final LocalDateTime asOf = null;
        final boolean test = false;
        Judgment judgment1 = new Judgment();
        Judgment judgment2 = new Judgment();

        judgment1.setJudgmentAdminOrderDate(LocalDate.now());
        judgment2.setJudgmentAdminOrderDate(LocalDate.now());

        List<Judgment> judgments = List.of(judgment1, judgment2);

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, SERVICE_ID_1, test);

        verify(mockSftpService).uploadFiles(anyList());
    }

    @Test
    void shouldDeleteFilesAfterUpload() {
        final LocalDateTime asOf = LocalDateTime.now();
        final boolean test = false;

        Judgment judgment1 = new Judgment();
        Judgment judgment2 = new Judgment();

        judgment1.setJudgmentAdminOrderDate(LocalDate.now());
        judgment2.setJudgmentAdminOrderDate(LocalDate.now());

        List<Judgment> judgments = List.of(judgment1, judgment2);

        File tmpDirFile = judgmentFileService.getTmpDirectory();

        File detailsFile = new File(tmpDirFile, getFileName(asOf, FILE_NAME_EXTENSION_DETAILS));
        File headerFile = new File(tmpDirFile, getFileName(asOf, FILE_NAME_EXTENSION_HEADER));

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, SERVICE_ID_1, test);

        assertFalse(detailsFile.exists(), "The data file should have been deleted after upload");
        assertFalse(headerFile.exists(), "The header file should have been deleted after upload");

        verify(mockSftpService).uploadFiles(anyList());

        tmpDirFile.deleteOnExit();
    }

    @Test
    void shouldFormatJudgmentsCorrectlyInDataFile() throws IOException {
        Judgment judgment1 = mock(Judgment.class);
        Judgment judgment2 = mock(Judgment.class);

        when(judgment1.toFormattedString()).thenReturn("JUDGMENT_1");
        when(judgment2.toFormattedString()).thenReturn("JUDGMENT_2");

        LocalDateTime asOf = LocalDateTime.now();

        judgmentFileService.createAndSendJudgmentFile(List.of(judgment1, judgment2), asOf, SERVICE_ID_1, true);

        File tmpDirFile = judgmentFileService.getTmpDirectory();
        Path detailsFilePath = Paths.get(tmpDirFile.getAbsolutePath(), getFileName(asOf, FILE_NAME_EXTENSION_DETAILS));
        Path headerFilePath = Paths.get(tmpDirFile.getAbsolutePath(), getFileName(asOf, FILE_NAME_EXTENSION_HEADER));

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

        verify(mockSftpService, never()).uploadFiles(anyList());
    }

    @Test
    void shouldNotUploadFilesInTestMode() {
        LocalDateTime asOf = LocalDateTime.now();
        boolean test = true;

        Judgment judgment = new Judgment();
        judgment.setJudgmentAdminOrderDate(LocalDate.now());

        List<Judgment> judgments = List.of(judgment);

        judgmentFileService.createAndSendJudgmentFile(judgments, asOf, SERVICE_ID_1, test);

        verify(mockSftpService, never()).uploadFiles(anyList());

        File tmpDirFile = judgmentFileService.getTmpDirectory();
        File detailsFile = new File(tmpDirFile, getFileName(asOf, FILE_NAME_EXTENSION_DETAILS));
        File headerFile = new File(tmpDirFile, getFileName(asOf, FILE_NAME_EXTENSION_HEADER));

        assertTrue(detailsFile.exists(), "Details file should still be created in test mode");
        assertTrue(headerFile.exists(), "Header file should still be created in test mode");
    }

    @Test
    void shouldHandleExceptionWhenDeletingFile() {
        Judgment judgment = new Judgment();
        judgment.setJudgmentAdminOrderDate(LocalDate.now());
        List<Judgment> judgments = List.of(judgment);

        LocalDateTime asOf = LocalDateTime.now();

        try (MockedStatic<Files> mockStaticFiles = mockStatic(Files.class)) {
            mockStaticFiles.when(() -> Files.delete(any(Path.class))).thenThrow(new IOException());

            DeleteFileException exception =
                assertThrows(DeleteFileException.class,
                             () -> judgmentFileService.createAndSendJudgmentFile(judgments, asOf, SERVICE_ID_1, false));

            String exceptionMessage = exception.getMessage();
            assertTrue(exceptionMessage.startsWith("Unable to delete file"),
                       "Exception has unexpected message: [" + exceptionMessage + "]");

            verify(mockSftpService).uploadFiles(anyList());
        }
    }

    private String getFileName(LocalDateTime asOf, String extension) {
        return String.format("judgment-%s-%s.%s", asOf.format(FILE_NAME_DATE_TIME_FORMAT), SERVICE_ID_1, extension);
    }
}
