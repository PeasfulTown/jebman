/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Metadata reader for epub, pdf formats, returns metadata such as document title,
 * isbn/uuid (or both if they exist), publish date, etc.
 */
package xyz.peasfultown.helpers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MetaReader {
    public static final String PATTERN_ISBN = "(97[89])?([0-9]){10}";
    public static final String PATTERN_UUID = "([a-zA-Z0-9]{8})-([a-zA-Z0-9]{4})-([a-zA-Z0-9]{4})-([a-zA-Z0-9]{4})-([a-zA-Z0-9]{12})";
    public static final String PATTERN_DATE = "[0-9]{4}-(0[1-9]|1[012])-(([0-2][0-9])|(3[01]))";
    public static final String PATTERN_ISO_DATETIME = "[0-9]{4}-(0[1-9]|1[012])-(([0-2][0-9])|(3[01]))T(([01][0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)??Z";
    public static final String PATTERN_ISO_DATETIME_OFFSET = "[0-9]{4}-(0[1-9]|1[012])-(([0-2][0-9])|(3[01]))T(([01][0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)??(\\+[0-5][0-9]:[0-5][0-9])";
    private static final String EPUB_META_FILE_NAME_PATTERN = ".*?(content.opf)$";

    public static HashMap<String, String> getMetadata(Path file) throws MetadataReaderException {
        HashMap<String, String> meta = new HashMap<>();
        setBasicFileProperties(meta, file);
        switch (meta.get("filetype").toLowerCase()) {
            case "epub":
                setEpubMetadata(file, meta);
                return meta;
            case "pdf":
                setPDFMetadata(file, meta);
                return meta;
            default:
                throw new MetadataReaderException("Cannot determine filetype.");
        }
    }

    public static void setPDFMetadata(Path file, HashMap<String, String> meta) throws MetadataReaderException {
        try (PDDocument pdf = PDDocument.load(file.toFile())) {
            PDDocumentInformation pdfInfo = pdf.getDocumentInformation();
            if (pdfInfo.getTitle() != null)
                meta.put("title", pdfInfo.getTitle());
            if (pdfInfo.getAuthor() != null)
                meta.put("author", pdfInfo.getAuthor());
            if (pdfInfo.getCreationDate() != null)
                meta.put("date", pdfInfo.getCreationDate().toInstant().truncatedTo(ChronoUnit.SECONDS).toString());
        } catch (Exception e) {
            throw new MetadataReaderException("Problem while setting getting PDF metadata.", e);
        }
    }

    public static void setEpubMetadata(Path file, HashMap<String, String> meta) throws MetadataReaderException {
        if (!Files.exists(file))
            throw new MetadataReaderException("File not found.");

        try {
            processXML(file.toFile(), meta);
        } catch (Exception e) {
            throw new MetadataReaderException(e.getMessage(), e);
        }
    }

    private static void setBasicFileProperties(Map<String, String> metadata, Path file) throws MetadataReaderException {
        String filename = file.getFileName().toString();
        String[] parts = filename.toString().split("\\.");
        if (parts.length < 2) {
            throw new MetadataReaderException(String.format("Unable to determine %s file type", file.getFileName()));
        }
        int ind = filename.lastIndexOf(".");
        metadata.putIfAbsent("filename", filename.substring(0, ind));
        metadata.putIfAbsent("filetype", filename.substring(ind + 1));
    }

    private static void processXML(File epub, HashMap<String, String> meta) throws XMLStreamException {
        XMLInputFactory xif = XMLInputFactory.newDefaultFactory();
        XMLStreamReader xsr = null;

        try (ZipFile zip = new ZipFile(epub);
             InputStream is = zip.getInputStream(getEpubMetaFile(zip))) {
            xsr = xif.createXMLStreamReader(is);
            processElements(xsr, meta);
        } catch (Exception e) {
            throw new XMLStreamException(e.getMessage(), e);
        } finally {
            if (xsr != null) {
                try {
                    xsr.close();
                } catch (XMLStreamException e) {
                    System.err.format("Unable to close XMLStreamReader: %s%n", e);
                }
            }
        }
    }

    private static void processElements(XMLStreamReader xsr, HashMap<String, String> meta) throws MetadataReaderException {
        String propName = null;
        try {
            while (xsr.hasNext()) {
                xsr.next();
                switch (xsr.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (xsr.getPrefix().equals("dc"))
                            propName = xsr.getLocalName();
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (xsr.hasText()) {
                            String parserText = xsr.getText();
                            if (propName != null && propName.equals("identifier")) {
                                boolean isUUID = Pattern.matches(MetaReader.PATTERN_UUID, parserText);
                                if (isUUID) {
                                    meta.putIfAbsent("uuid", parserText);
                                } else {
                                    meta.putIfAbsent("isbn", parserText);
                                }
                            } else {
                                meta.putIfAbsent(propName, parserText);
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        // End process when reach end of metadata tag no need to process further
                        if (xsr.getLocalName().equals("metadata")) {
                            return;
                        }
                        break;
                    default:
                        // Do nothing
                        break;
                }
            }
        } catch (Exception e) {
            throw new MetadataReaderException(e.getMessage(), e);
        }
    }

    public static Instant parseDate(String date) {
        if (Pattern.matches(PATTERN_DATE, date)) {
            return LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC);
        } else if (Pattern.matches(PATTERN_ISO_DATETIME, date)) {
            return Instant.parse(date);
        } else if (Pattern.matches(PATTERN_ISO_DATETIME_OFFSET, date)) {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(date, Instant::from);
        }

        return Instant.now().truncatedTo(ChronoUnit.DAYS);
    }

    private static ZipEntry getEpubMetaFile(ZipFile zipFile) {
        Enumeration elems = zipFile.entries();

        while (elems.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) elems.nextElement();
            if (Pattern.matches(".*?content.opf$", ze.getName())) {
                return ze;
            }
        }

        return null;
    }

    /**
     * SAX parser method to get metadata from epub.
     *
     * @param filePath the absolute path to file.
     * @return Hashmap containing the epub metadata.
     * @throws InvalidPathException if the provided file path is invalid.
     * @throws SAXException         if any SAX exception occurs during processing.
     * @throws IOException          if the file at the provided filepath does not exist or the parser failed to parse the file.
     */
    public static HashMap getEpubMetadataSAX(String filePath) throws InvalidPathException, SAXException, IOException {

        Path file = null;

        try {
            file = Path.of(filePath).toAbsolutePath();
        } catch (InvalidPathException e) {
            throw new InvalidPathException(filePath, "Provided file path is invalid.");
        }

        if (!file.isAbsolute()) {
            throw new InvalidPathException(filePath, "Absolute file path is required.");
        }

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            throw new FileNotFoundException("Provided file does not exist or is not a regular file.\n");
        }

        // TODO (feat): check filetype before creating instantiating ZipFile object

        ZipFile epubZip = new ZipFile(file.toFile());
        ZipEntry epubMetaFile = getEpubMetaFile(epubZip);

        if (epubMetaFile == null) {
            throw new FileNotFoundException("Failed to find Epub metadata file.");
        }

        InputStream epubMetaInputStream = epubZip.getInputStream(epubMetaFile);

        SAXParser saxParser = null;
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);

            saxParser = saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            return null;
        } catch (SAXException e) {
            throw new SAXException("Creating SAXParser object in MetaReader class failed.\n" + e.getMessage());
        }

        HashMap meta = null;
        try {
            EpubMetadataSAXHandler metaHandler = new EpubMetadataSAXHandler();
            saxParser.parse(epubMetaInputStream, metaHandler);
            meta = metaHandler.getResults();
        } catch (IOException e) {
            throw new IOException("Epub parsing operation failed.\n" + e.getMessage());
        } finally {
            if (epubMetaInputStream != null) {
                epubMetaInputStream.close();
            }
        }

        return meta;
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
