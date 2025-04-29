package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.domain.Judgment;
import uk.gov.hmcts.reform.civil.repository.JudgmentRepository;
import uk.gov.hmcts.reform.civil.util.LocalSftpServer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.DIR_TYPE_REMOTE;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertFileInDir;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertNoFilesInDir;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertNumFilesInDir;

@SpringBootTest
@ActiveProfiles("itest")
@Transactional
@Sql("scheduled_report_service_int_test.sql")
class ScheduledReportServiceIntTest {

    private static final boolean NOT_TEST = false;
    private static final boolean IS_TEST = true;

    private static final String SERVICE_ID_1 = "IT01";
    private static final String SERVICE_ID_2 = "IT02";

    private static final String FILE_EXTENSION_DETAILS = ".det";
    private static final String FILE_EXTENSION_HEADER = ".hdr";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private static final LocalDateTime AS_OF_DATE_TIME = LocalDateTime.of(2025, 1, 1, 16, 47, 23);

    private final int sftpPort;
    private final String sftpRemoteDir;

    @TempDir
    private File sftpRootDir;

    private final JudgmentRepository judgmentRepository;

    private final ScheduledReportService scheduledReportService;

    @Autowired
    public ScheduledReportServiceIntTest(ScheduledReportService scheduledReportService,
                                         JudgmentRepository judgmentRepository,
                                         @Value("${rtl-export.sftp.serverPort}") int sftpPort,
                                         @Value("${rtl-export.sftp.remoteDir}") String sftpRemoteDir) {
        this.scheduledReportService = scheduledReportService;
        this.judgmentRepository = judgmentRepository;
        this.sftpPort = sftpPort;
        this.sftpRemoteDir = sftpRemoteDir;
    }

    @Test
    void testGenerateReport() throws IOException {
        LocalDate currentDate = LocalDate.now();

        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledReportService.generateReport(NOT_TEST, null, null);

            File remoteDir = sftpServer.getRemoteDir();
            assertNumFilesInDir(remoteDir, DIR_TYPE_REMOTE, 4);
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            SERVICE_ID_1 + FILE_EXTENSION_HEADER,
                            List.of(getExpectedHeaderFileContent(currentDate)));
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            SERVICE_ID_1 + FILE_EXTENSION_DETAILS,
                            getExpectedDetailsFileContent("1", new BigDecimal("1.11"), LocalDate.of(2024, 1, 1)));
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            SERVICE_ID_2 + FILE_EXTENSION_HEADER,
                            List.of(getExpectedHeaderFileContent(currentDate)));
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            SERVICE_ID_2 + FILE_EXTENSION_DETAILS,
                            getExpectedDetailsFileContent("3", new BigDecimal("3.33"), LocalDate.of(2024, 3, 3)));
            assertReportedToRtlDateNotNull(List.of(1L, 3L));
        }
    }

    @ParameterizedTest
    @MethodSource("generateReportTestModeParams")
    void testGenerateReportTestMode(LocalDateTime asOf, String serviceId, List<Long> ids) throws IOException {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledReportService.generateReport(IS_TEST, asOf, serviceId);

            File remoteDir = sftpServer.getRemoteDir();
            assertNoFilesInDir(remoteDir, DIR_TYPE_REMOTE);

            if (asOf == null) {
                assertReportedToRtlDateNull(ids);
            } else {
                assertReportedToRtlDate(ids, asOf);
            }
        }
    }

    @Test
    void testGenerateReportServiceId() throws IOException {
        LocalDate currentDate = LocalDate.now();

        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledReportService.generateReport(NOT_TEST, null, SERVICE_ID_2);

            File remoteDir = sftpServer.getRemoteDir();
            assertNumFilesInDir(remoteDir, DIR_TYPE_REMOTE, 2);
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            SERVICE_ID_2 + FILE_EXTENSION_HEADER,
                            List.of(getExpectedHeaderFileContent(currentDate)));
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            SERVICE_ID_2 + FILE_EXTENSION_DETAILS,
                            getExpectedDetailsFileContent("3", new BigDecimal("3.33"), LocalDate.of(2024, 3, 3)));
            assertReportedToRtlDateNotNull(List.of(3L));
        }
    }

    @Test
    void testGenerateReportAsOf() throws IOException {
        LocalDate headerDate = AS_OF_DATE_TIME.toLocalDate();
        String baseFileName = "judgment-2025-01-01-16-47-23-";

        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledReportService.generateReport(NOT_TEST, AS_OF_DATE_TIME, null);

            File remoteDir = sftpServer.getRemoteDir();
            assertNumFilesInDir(remoteDir, DIR_TYPE_REMOTE, 4);
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            baseFileName + SERVICE_ID_1 + FILE_EXTENSION_HEADER,
                            List.of(getExpectedHeaderFileContent(headerDate)));
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            baseFileName + SERVICE_ID_1 + FILE_EXTENSION_DETAILS,
                            getExpectedDetailsFileContent("2", new BigDecimal("2.22"), LocalDate.of(2024, 2, 2)));
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            baseFileName + SERVICE_ID_2 + FILE_EXTENSION_HEADER,
                            List.of(getExpectedHeaderFileContent(headerDate)));
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            baseFileName + SERVICE_ID_2 + FILE_EXTENSION_DETAILS,
                            getExpectedDetailsFileContent("4", new BigDecimal("4.44"), LocalDate.of(2024, 4, 4)));
            assertReportedToRtlDate(List.of(2L, 4L), AS_OF_DATE_TIME);
        }
    }

    @Test
    void testGenerateReportAsOfServiceId() throws IOException {
        LocalDate headerDate = AS_OF_DATE_TIME.toLocalDate();
        String baseFileName = "judgment-2025-01-01-16-47-23-";

        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            scheduledReportService.generateReport(NOT_TEST, AS_OF_DATE_TIME, SERVICE_ID_2);

            File remoteDir = sftpServer.getRemoteDir();
            assertNumFilesInDir(remoteDir, DIR_TYPE_REMOTE, 2);
            assertFileInDir(remoteDir,
                            DIR_TYPE_REMOTE,
                            baseFileName + SERVICE_ID_2 + FILE_EXTENSION_HEADER,
                            List.of(getExpectedHeaderFileContent(headerDate)));
            assertFileInDir(remoteDir,
                            baseFileName + SERVICE_ID_2 + FILE_EXTENSION_DETAILS,
                            getExpectedDetailsFileContent("4", new BigDecimal("4.44"), LocalDate.of(2024, 4, 4)));
            assertReportedToRtlDate(List.of(4L), AS_OF_DATE_TIME);
        }
    }

    private String getExpectedHeaderFileContent(LocalDate date) {
        return "1         " + date.format(DATE_FORMAT);
    }

    private List<String> getExpectedDetailsFileContent(String num, BigDecimal orderTotal, LocalDate orderDate) {
        // Repeated num character in test data utilised to reduce number of parameters
        String fileLine =  num.repeat(3)
            + "CASE" + num.repeat(4)
            + String.format("%011.2f", orderTotal)
            + orderDate.format(DATE_FORMAT)
            + "R"
            + "        "
            + "Defendant Name " + num + "                                                      "
            + "Address " + num + " Line 1                   "
            + "                                   "
            + "                                   "
            + "                                   "
            + "                                   "
            + "AA" + num + " " + num + "AA "
            + "        ";
        return List.of(fileLine);
    }

    private void assertReportedToRtlDate(List<Long> ids, LocalDateTime date) {
        List<Judgment> judgments = judgmentRepository.findAllById(ids);
        for (Judgment judgment : judgments) {
            assertEquals(date,
                         judgment.getReportedToRtl(),
                         "Judgment id [" + judgment.getId() + "] has an unexpected reported to RTL date");
        }
    }

    private void assertReportedToRtlDateNull(List<Long> ids) {
        List<Judgment> judgments = judgmentRepository.findAllById(ids);
        for (Judgment judgment : judgments) {
            assertNull(judgment.getReportedToRtl(),
                       "Judgment with id [" + judgment.getId() + "] should have a null reported to RTL date");
        }
    }

    private void assertReportedToRtlDateNotNull(List<Long> ids) {
        List<Judgment> judgments = judgmentRepository.findAllById(ids);
        for (Judgment judgment : judgments) {
            assertNotNull(judgment.getReportedToRtl(),
                          "Judgment with id [" + judgment.getId() + "] should not have a null reported to RTL date");
        }
    }

    private static Stream<Arguments> generateReportTestModeParams() {
        return Stream.of(
            arguments(null, null, List.of(1L, 3L)),
            arguments(AS_OF_DATE_TIME, null, List.of(2L, 4L)),
            arguments(null, SERVICE_ID_1, List.of(1L)),
            arguments(AS_OF_DATE_TIME, SERVICE_ID_2, List.of(4L))
        );
    }
}
