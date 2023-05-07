/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Tests for MetaReader.
 */
package xyz.peasfultown;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

// TODO (tweak): Probe filetype by checking extension
public class MetaReaderTest {
    private static final Logger logger = LoggerFactory.getLogger(MetaReaderTest.class);

    // Book files
    private String gatsby = getClass().getClassLoader().getResource("gatsby.epub").getFile();
    private String frankenstein = getClass().getClassLoader().getResource("frankenstein.epub").getFile();

    @Test
    void getEpubMetaUsingSAX() {
        logger.info("=== Fetching epub metadata using SAX handler");
        HashMap meta = null;
        try {
            meta = MetaReader.getEpubMetadataSAX(gatsby);
        } catch (Exception e) {
            logger.error("Fetching metadata failed.", e);

            fail(e);
        }

        assertEquals("Francis Scott Fitzgerald", meta.get("author"));
        assertEquals("The Great Gatsby", meta.get("title"));
        assertEquals("2010-03-02T23:12:20.748000+00:00", meta.get("date"));
        assertEquals("40b74d9e-9c86-4593-968b-8b4101013ce2", meta.get("identifier"));
        assertEquals("Scribner", meta.get("publisher"));
        assertEquals("SUMMARY:\nMany consider The Great Gatsby the closest thing to the Great American Novel ever " +
                "written. First published in 1925, it is the timeless story of Jay Gatsby and his love for Daisy " +
                "Buchanan. Gatsby lives in the New York suburb of West Egg, where those with \"", meta.get("description"));
    }

    @Test
    void getEpubMetaUsingStAX1() throws IOException, XMLStreamException {
        Path file = getFileFromResources("gatsby.epub");
        HashMap<String, String> meta = MetaReader.getEpubMetadata(file);

        logger.info("=== Checking \"{}\" metadata", file);

        assertNotNull(meta);
        assertEquals("40b74d9e-9c86-4593-968b-8b4101013ce2", meta.get("uuid"));
        assertEquals("The Great Gatsby", meta.get("title"));
        assertEquals("Francis Scott Fitzgerald", meta.get("creator"));
        assertEquals("2010-03-02T23:12:20.748000+00:00", meta.get("date"));
        assertEquals("Scribner", meta.get("publisher"));
        assertEquals("SUMMARY:\n" +
                        "Many consider The Great Gatsby the closest thing to the Great American Novel ever written. " +
                        "First published in 1925, it is the timeless story of Jay Gatsby and his love for Daisy Buchanan. " +
                        "Gatsby lives in the New York suburb of West Egg, where those with \"",
                meta.get("description"));
    }

    @Test
    void getEpubMetaUsingStAX2() throws IOException, XMLStreamException {
        Path file = getFileFromResources("frankenstein.epub");

        HashMap<String, String> meta = MetaReader
                .getEpubMetadata(file);

        logger.info("=== Checking \"{}\" metadata", file);

        assertNotNull(meta);
        meta.forEach((key, val) -> {
            logger.info("{} = {}", key, val);
        });
        assertEquals("9780199537150", meta.get("isbn"));
        assertEquals("3f2fdc96-e5f6-430f-a08a-8c6c9dc8341c", meta.get("uuid"));
        assertEquals("Frankenstein", meta.get("title"));
        assertEquals("2009-08-15T07:00:00+00:00", meta.get("date"));
        assertEquals("Mary Wollstonecraft Shelley", meta.get("creator"));
        assertEquals("Oxford University Press", meta.get("publisher"));
        assertEquals("SUMMARY: Shelley's enduringly popular and rich gothic tale confronts some of the most feared " +
                "innovations of evolutionism and science--topics such as degeneracy, hereditary disease, and humankind's " +
                "ability to act as creator of the modern world. This new edition, based on the harder and wittier 1818 " +
                "version of the text, draws on new research and examines the novel in the context of the controversial " +
                "radical sciences developing in the years following the Napoleonic Wars, and shows the relationship of " +
                "Frankenstein's experiment to the contemporary debate between champions of materialistic science and " +
                "proponents of received religion.", meta.get("description"));
    }

    @Test
    void getPDFMeta1() {
        Path file = getFileFromResources("machine-stops.pdf");
        HashMap<String, String> meta = null;

        logger.info("=== Checking \"{}\" metadata", file.toAbsolutePath().toString());
        try {
            meta = MetaReader.getPDFMetadata(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e);
        }

        assertEquals("The-Machine-Stops.dvi", meta.get("title"));
        assertEquals("2008-06-05T17:14:45Z", meta.get("date"));
    }

    @Test
    void getPDFMeta2() {
        Path file = getFileFromResources("dummy.pdf");
        logger.info("=== Checking \"{}\" metadata", file.toAbsolutePath().toString());
        HashMap<String, String> meta = new HashMap<>();
        try  {
            meta = MetaReader.getPDFMetadata(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e);
        }

        logger.info("Filename: {}", file.getFileName().toString());
        assertNull(meta.get("title"));
        assertEquals("Evangelos Vlachogiannis", meta.get("author"));
        assertEquals("2007-02-23T15:56:37Z", meta.get("date"));
    }

    String getEventTypeString(int eventType) {
        switch (eventType) {
            case XMLStreamConstants.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLStreamConstants.DTD:
                return "DTD";
            case XMLStreamConstants.CDATA:
                return "CDATA";
            case XMLStreamConstants.CHARACTERS:
                return "CHARACTERS";
            case XMLStreamConstants.COMMENT:
                return "COMMENT";
            case XMLStreamConstants.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLStreamConstants.END_ELEMENT:
                return "END_ELEMENT";
            case XMLStreamConstants.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLStreamConstants.START_ELEMENT:
                return "START_ELEMENT";
            case XMLStreamConstants.ENTITY_DECLARATION:
                return "ENTITY_DECLARATION";
            case XMLStreamConstants.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLStreamConstants.NAMESPACE:
                return "NAMESPACE";
            case XMLStreamConstants.NOTATION_DECLARATION:
                return "NOTATION_DECLARATION";
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLStreamConstants.SPACE:
                return "SPACE";
        }

        return "undefined";
    }

    Path getFileFromResources(String filename) {
        Path file = Path.of(getClass().getClassLoader().getResource(filename).getFile());

        return file;
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
