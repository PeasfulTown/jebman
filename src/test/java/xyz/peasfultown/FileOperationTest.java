/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: tests for FileOperation class.
 */
package xyz.peasfultown;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.*;

/**
 * File operations such as copying, removing, and getting basic file attributes.
 * TODO: FINISH
 */
class FileOperation {
    private static final String USER_HOME_PROP_NAME = "user.home";

    public static BasicFileAttributes getBasicFileAttributes(Path file) throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);

        return attrs;
    }

    /**
     * Copy file to the provided destination directory.
     * @param orig absolute path to file to copy.
     * @param dest destination directory to copy file to.
     */
    public static void copy(Path orig, Path dest) throws IOException {
        Path newFile = (Files.isDirectory(dest)) ? dest.resolve(orig.getFileName()) : dest;

        if (!Files.exists(orig)) {
            throw new IOException ("File does not exist.");
        }

        if (!Files.isDirectory(dest)) {
            throw new IOException ("Destination path should be a directory.");
        }

        Files.copy(orig, dest);
    }

    private static Path getUserHomeDirectory() {
        return Path.of(System.getProperty(USER_HOME_PROP_NAME));
    }

    private static Path getUserDocumentsDirectory() {
        return getUserHomeDirectory().resolve("Documents");
    }

    public static double getFileSize(String filePath) {
        return 0;
    }
}

@TestMethodOrder(OrderAnnotation.class)
class FileOperationTest {
    private static final Logger logger = LoggerFactory.getLogger(FileOperationTest.class);
    private Path tempDir;

    @Test
    @Disabled
    void getBasicFileAttributes() {
        Path file = Path.of(getClass().getClassLoader().getResource("gatsby.epub").getFile());
        BasicFileAttributes attrs = null;

        try {
            attrs = FileOperation.getBasicFileAttributes(file);
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("\nFile creation time: {}\n-Is directory: {}\nLast modified: {}\n",
                attrs.creationTime().toInstant().toString(),
                attrs.isDirectory(),
                attrs.lastModifiedTime().toInstant().toString());

        assertNotNull(attrs);
    }

    @Test
    @Disabled
    @Order(1)
    void copyFile() throws IOException {
        logger.info("Testing copying file");
        Path file = getFileFromResources("dummy.pdf");
        try {
            if (tempDir != null && !Files.exists(tempDir))
                tempDir = Files.createTempDirectory("jebman");

            logger.info("File name: {}", file.getFileName());
            logger.info("Temp dir: {}", tempDir.toString());
            assertTrue(Files.isDirectory(tempDir));
            FileOperation.copy(file, tempDir);
            assertTrue(Files.exists(tempDir.resolve(file.getFileName())));
        } catch (IOException e) {
            logger.error(e.getMessage());
            fail(e);
        }
    }

    @Order(2)
    @Disabled
    void removeFile() {
        logger.info("Testing removing file and directory");
        assertTrue(Files.isDirectory(tempDir));
        assertTrue(Files.exists(tempDir.resolve("dummy.pdf")));
    }

    Path getFileFromResources(String filename) {
        return Path.of(getClass().getClassLoader().getResource(filename).getFile());
    }
}

/**
 * The MIT License (MIT)
 * =====================
 * <p>
 * Copyright © 2023 PeasfulTown
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
