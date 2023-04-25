/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Metadata reader for epub, pdf formats, returns metadata such as document title,
 * isbn/uuid (or both if they exist), publish date, etc.
 */
package xyz.peasfultown;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class MetaReader {
    public static final String PATTERN_ISBN = "(97[89])?([0-9]){10}";
    public static final String PATTERN_UUID = "([a-zA-Z0-9]{8})-([a-zA-Z0-9]{4})-([a-zA-Z0-9]{4})-([a-zA-Z0-9]{4})-([a-zA-Z0-9]{12})";
    public static final String PATTERN_DATE = "[0-9]{4}-(0[1-9]|1[012])-(([0-2][0-9])|(3[01]))";
    public static final String PATTERN_ISO_DATETIME = "[0-9]{4}-(0[1-9]|1[012])-(([0-2][0-9])|(3[01]))T(([01][0-9])|(2[0-3])):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)??(Z|\\+[0-5][0-9]:[0-5][0-9])";

    private static final String EPUB_META_FILE_NAME_PATTERN = ".*?(content.opf)$";

    /**
     * Prevent object instantiation.
     */
    protected MetaReader() {
    }

    public static HashMap<String, String> getEpubMetadata(String filePath) throws IOException, XMLStreamException {
        Path file = Path.of(filePath);

        if (!Files.exists(file)) {
            throw new FileNotFoundException("File not found.");
        }

        // Create input stream from zip entry
        ZipFile epubZip = null;
        InputStream inputStream = null;

        XMLStreamReader xsr = null;
        HashMap<String, String> meta = new HashMap<>();

        try {
            epubZip = new ZipFile(file.toFile());
            inputStream = epubZip.getInputStream(getEpubMetaFile(epubZip));

            XMLInputFactory xif = XMLInputFactory.newDefaultFactory();
            xsr = xif.createXMLStreamReader(inputStream);

            String propName = null;
            boolean keepProp = false;
            int iIsbn = 0;
            int iUuid = 0;
            while (xsr.hasNext()) {
                xsr.next();

                if (xsr.isStartElement()) {
                    if (xsr.getPrefix().equals("dc")) {
                        propName = xsr.getLocalName();
                        keepProp = true;
                    }
                }

                if (xsr.isCharacters() && keepProp) {
                    if (xsr.hasText()) {
                        String parserText = xsr.getText();
                        if (propName.equals("identifier")) {
                            boolean isUUID = Pattern.matches(MetaReader.PATTERN_UUID, parserText);

                            if (isUUID) {
                                meta.putIfAbsent(String.format("uuid%s", iUuid), parserText);
                                iUuid++;
                            } else {
                                meta.putIfAbsent(String.format("isbn%s", iIsbn), parserText);
                                iIsbn++;
                            }
                        } else {
                            meta.putIfAbsent(propName, parserText);
                        }
                    }

                    keepProp = false;
                }

                // End parsing job when the metadata tag is ended
                if (xsr.isEndElement()) {
                    if (xsr.getLocalName().equals("metadata")) {
                        break;
                    }
                }
            }
        } finally {
            if (xsr != null) {
                xsr.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

            if (epubZip != null) {
                epubZip.close();
            }
        }
        return meta;
    }

    public static HashMap getPDFMetadata(String filepath) throws IOException {
        HashMap<String, String> metadata = new HashMap<>();

        try (PDDocument pdf = PDDocument.load(Path.of(filepath).toFile())) {
            PDDocumentInformation pdfInfo = pdf.getDocumentInformation();
            metadata.put("title", pdfInfo.getTitle());
            metadata.put("author", pdfInfo.getAuthor());
            metadata.put("date", pdfInfo.getCreationDate().toInstant().truncatedTo(ChronoUnit.SECONDS).toString());
        }

        return metadata;
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
