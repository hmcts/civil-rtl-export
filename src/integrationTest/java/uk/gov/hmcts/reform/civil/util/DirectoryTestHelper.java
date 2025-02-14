package uk.gov.hmcts.reform.civil.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryTestHelper {

    public static final String DIR_TYPE_DEFAULT = "Directory";
    public static final String DIR_TYPE_TEMP = "Temp directory";
    public static final String DIR_TYPE_REMOTE = "Remote directory";

    private DirectoryTestHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static void assertNoFilesInDir(File dir) {
        assertNumFilesInDir(dir, 0);
    }

    public static void assertNoFilesInDir(File dir, String dirType) {
        assertNumFilesInDir(dir, dirType, 0);
    }

    public static void assertNumFilesInDir(File dir, int expectedNumFiles) {
        assertNumFilesInDir(dir, DIR_TYPE_DEFAULT, expectedNumFiles);
    }

    public static void assertNumFilesInDir(File dir, String dirType, int expectedNumFiles) {
        File[] filesInDir = dir.listFiles();
        assertDirListingNotNull(filesInDir, dirType);
        assertEquals(expectedNumFiles, filesInDir.length, dirType + " contains unexpected number of files");
    }

    public static void assertFileInDir(File dir, String fileName, List<String> expectedFileContent) throws IOException {
        assertFileInDir(dir, DIR_TYPE_DEFAULT, fileName, expectedFileContent);
    }

    public static void assertFileInDir(File dir, String dirType, String fileName, List<String> expectedFileLines)
        throws IOException {
        FilenameFilter filter = (directory, name) -> name.endsWith(fileName);

        File[] filesInDir = dir.listFiles(filter);
        assertDirListingNotNull(filesInDir, dirType);
        assertEquals(1, filesInDir.length, dirType + " should contain file " + fileName);

        List<String> fileLines = Files.readAllLines(filesInDir[0].toPath());
        assertEquals(expectedFileLines.size(), fileLines.size(), fileName + " contains unexpected number of lines");

        for (int lineNum = 0; lineNum < expectedFileLines.size(); lineNum++) {
            assertEquals(expectedFileLines.get(lineNum),
                         fileLines.get(lineNum),
                         dirType + ": " + fileName + " does not contain expected content at line " + lineNum);
        }
    }

    public static void assertFileNamesInDir(File dir, String dirType, List<String> expectedFileNames) {
        String[] fileNamesInDir = dir.list();
        assertNotNull(fileNamesInDir, dirType + " listing should not be null");

        assertEquals(expectedFileNames.size(),
                     fileNamesInDir.length,
                     dirType + " contains unexpected number of files");

        List<String> fileNames = Arrays.asList(fileNamesInDir);
        for (String expectedFileName : expectedFileNames) {
            assertTrue(fileNames.stream().anyMatch(name -> name.endsWith(expectedFileName)),
                       dirType + " should contain file " + expectedFileName);
        }
    }

    private static void assertDirListingNotNull(File[] dirListing, String dirType) {
        assertNotNull(dirListing, dirType + " listing should not be null");
    }
}
