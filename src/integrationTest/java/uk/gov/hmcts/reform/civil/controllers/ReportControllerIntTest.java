package uk.gov.hmcts.reform.civil.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.civil.util.LocalSftpServer;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.DIR_TYPE_REMOTE;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertFileNamesInDir;
import static uk.gov.hmcts.reform.civil.util.DirectoryTestHelper.assertNoFilesInDir;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("itest")
@Transactional
@Sql("report_controller_int_test.sql")
class ReportControllerIntTest {

    private static final String URL_REPORT = "/trigger-report";

    private static final String QUERY_PARAM_AS_OF = "asOf";
    private static final String QUERY_PARAM_TEST = "test";
    private static final String QUERY_PARAM_SERVICE_ID = "serviceId";

    private static final LocalDateTime DATE_TIME_AS_OF = LocalDateTime.of(2024, 10, 22, 12, 0, 0);

    private static final String SERVICE_ID_1 = "IT01";
    private static final String SERVICE_ID_2 = "IT02";
    private static final String SERVICE_ID_3 = "IT03";
    private static final String SERVICE_ID_4 = "IT04";

    private static final boolean IS_TEST = true;

    private static final String RESPONSE_SUCCESS = "Report has completed successfully";

    @TempDir
    private File sftpRootDir;

    private final int sftpPort;
    private final String sftpRemoteDir;

    private final MockMvc mockMvc;

    @Autowired
    public ReportControllerIntTest(MockMvc mockMvc,
                                   @Value("${rtl-export.sftp.serverPort}") int sftpPort,
                                   @Value("${rtl-export.sftp.remoteDir}") String sftpRemoteDir) {
        this.mockMvc = mockMvc;
        this.sftpPort = sftpPort;
        this.sftpRemoteDir = sftpRemoteDir;
    }

    @Test
    void testReportNoParameters() throws Exception {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            mockMvc.perform(get(URL_REPORT))
                .andExpect(status().isOk())
                .andExpect(content().string(RESPONSE_SUCCESS));

            File remoteDir = sftpServer.getRemoteDir();
            assertFileNamesInDir(remoteDir, DIR_TYPE_REMOTE, List.of("IT01.hdr", "IT01.det", "IT03.hdr", "IT03.det"));
        }
    }

    @Test
    void testReportAsOf() throws Exception {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            mockMvc.perform(get(URL_REPORT).queryParams(getQueryParams(DATE_TIME_AS_OF, null, null)))
                .andExpect(status().isOk())
                .andExpect(content().string(RESPONSE_SUCCESS));

            File remoteDir = sftpServer.getRemoteDir();
            assertFileNamesInDir(remoteDir, DIR_TYPE_REMOTE, List.of("IT02.hdr", "IT02.det", "IT04.hdr", "IT04.det"));
        }
    }

    @Test
    void testReportServiceId() throws Exception {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            mockMvc.perform(get(URL_REPORT).queryParams(getQueryParams(null, null, SERVICE_ID_3)))
                .andExpect(status().isOk())
                .andExpect(content().string(RESPONSE_SUCCESS));

            File remoteDir = sftpServer.getRemoteDir();
            assertFileNamesInDir(remoteDir, DIR_TYPE_REMOTE, List.of("IT03.hdr", "IT03.det"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "AA", "AAA", "AAAAA", "aaaa", "1", "11", "111", "11111", "_"})
    void testReportInvalidServiceId(String serviceId) throws Exception {
        mockMvc.perform(get(URL_REPORT).queryParams(getQueryParams(null, null, serviceId)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void testReportAsOfServiceId() throws Exception {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            mockMvc.perform(get(URL_REPORT).queryParams(getQueryParams(DATE_TIME_AS_OF, null, SERVICE_ID_4)))
                .andExpect(status().isOk())
                .andExpect(content().string(RESPONSE_SUCCESS));

            File remoteDir = sftpServer.getRemoteDir();
            assertFileNamesInDir(remoteDir, DIR_TYPE_REMOTE, List.of("IT04.hdr", "IT04.det"));
        }
    }

    @Test
    void testReportNoJudgments() throws Exception {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            mockMvc.perform(get(URL_REPORT).queryParams(getQueryParams(DATE_TIME_AS_OF, null, SERVICE_ID_1)))
                .andExpect(status().isOk())
                .andExpect(content().string(RESPONSE_SUCCESS));

            File remoteDir = sftpServer.getRemoteDir();
            assertNoFilesInDir(remoteDir, DIR_TYPE_REMOTE);
        }
    }

    @ParameterizedTest
    @MethodSource("reportTestModeParams")
    void testReportTestMode(LocalDateTime asOf, String serviceId) throws Exception {
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort, sftpRootDir, sftpRemoteDir)) {
            mockMvc.perform(get(URL_REPORT).queryParams(getQueryParams(asOf, IS_TEST, serviceId)))
                .andExpect(status().isOk())
                .andExpect(content().string(RESPONSE_SUCCESS));

            File remoteDir = sftpServer.getRemoteDir();
            assertNoFilesInDir(remoteDir, DIR_TYPE_REMOTE);
        }
    }

    @Test
    void testReportHandlesException() throws Exception {
        // Create an SFTP server on a different port to simulate a connection failure and trigger an exception
        try (LocalSftpServer sftpServer = LocalSftpServer.create(sftpPort + 1, sftpRootDir, sftpRemoteDir)) {
            mockMvc.perform(get(URL_REPORT).queryParams(getQueryParams(DATE_TIME_AS_OF, null, SERVICE_ID_2)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(equalTo("Report failed")));

            File remoteDir = sftpServer.getRemoteDir();
            assertNoFilesInDir(remoteDir, DIR_TYPE_REMOTE);
        }
    }

    private MultiValueMap<String, String> getQueryParams(LocalDateTime asOf, Boolean test, String serviceId) {
        MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>();
        if (asOf != null) {
            queryParamMap.put(QUERY_PARAM_AS_OF,
                              Collections.singletonList(asOf.format(DateTimeFormatter.ISO_DATE_TIME))
            );
        }
        if (test != null) {
            queryParamMap.put(QUERY_PARAM_TEST, Collections.singletonList(test.toString()));
        }
        if (serviceId != null) {
            queryParamMap.put(QUERY_PARAM_SERVICE_ID, Collections.singletonList(serviceId));
        }

        return queryParamMap;
    }

    private static Stream<Arguments> reportTestModeParams() {
        return Stream.of(
            arguments(null, null),
            arguments(DATE_TIME_AS_OF, null),
            arguments(null, SERVICE_ID_1),
            arguments(DATE_TIME_AS_OF, SERVICE_ID_2)
        );
    }
}
