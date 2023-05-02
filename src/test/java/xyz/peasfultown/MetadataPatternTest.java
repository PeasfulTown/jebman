/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Tests for MetaReader regex patterns.
 */
package xyz.peasfultown;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for MetaReader class for validating fields in EPUB metadata file
 * because sometimes they are formatted weirdly and inconsistently.
 *
 * === Identifiers ===
 * Book identifiers in epub metadata file can be:
 * - 10 digits or 13 digits ISBN format.
 * - UUID format.
 * Some epub files have both.
 * Sometimes they also contain multiple different ISBNs.
 *
 * These identifiers are sometimes have the prefix
 * `urn:isbn:`
 * or
 * `urn:uuid:`
 *
 * === Date ===
 * Date format in epub metadata file can be one of these formats:
 * - yyyy-mm-dd
 * - yyyy-mm-ddTHH:MM:SSZ
 * - Possibly other formats of ISO8601
 *
 * === Creator ===
 * Book author in epub.
 *
 * Sometimes author names in epub metadata are formatted as
 * - Lastname, Firstname
 * - Firstname Lastname, YearInOperation
 *
 * TODO: test for `urn` prefixes
 * https://en.wikipedia.org/wiki/Uniform_Resource_Name#Examples
 */
public class MetadataPatternTest {
    private static final Logger logger = LoggerFactory.getLogger(MetadataPatternTest.class);

    @Test
    void testISBNPattern() {
        String pattern = MetaReader.PATTERN_ISBN;

        String[] isbns = new String[] {
                "9780199537150",
                "9781718500662",
                "1718500661",
                "1449339530",
                "9781492025795",
                "9780062470973",
        };

        String[] fake = new String[] {
                "389238",
                "97842324243",
        };

        for (int i = 0; i < isbns.length; i++) {
            boolean match = Pattern.matches(pattern, isbns[i]);
            assertTrue(match, isbns[i] + " should match the regex pattern.");
        }

        for (int i = 0; i < fake.length; i++) {
            boolean match = Pattern.matches(pattern, fake[i]);
            assertFalse(match, fake[i] + " should not match isbn regex pattern.");
        }
    }

    @Test
    void testUUIDPattern() {
        String pattern = MetaReader.PATTERN_UUID;

        String[] uuids = new String[] {
                "3f2fdc96-e5f6-430f-a08a-8c6c9dc8341c",
                "40b74d9e-9c86-4593-968b-8b4101013ce2",
        };

        String[] fake = new String[] {
                "jsdfj-fjdfddfe-aiueri-ueiruieru",
                "923834-234234-38293-234234",
        };

        for (int i = 0; i < uuids.length; i++) {
            boolean match = Pattern.matches(pattern, uuids[i]);
            assertTrue(match, uuids[i] + " should match the uuid pattern.");
        }

        for (int i = 0; i < fake.length; i++) {
            boolean match = Pattern.matches(pattern, fake[i]);
            assertFalse(match, fake[i] + " should not match the uuid pattern.");
        }
    }

    /**
     * Patterns for ISO date formats and non-ISO date formats in order to correctly convert into Instant objects.
     */
    @Test
    void testDatePattern() {
        // YYYY-MM-DD format
        String pattern1 = MetaReader.PATTERN_DATE;
        // ISO8601 format
        // yyyy-mm-ddTHH:MM:SSZ
        // TODO: add extra checks for different ISO formats
        String pattern2 = MetaReader.PATTERN_ISO_DATETIME;
        // yyyy-mm-ddTHH:MM:SS+HH:MM
        String pattern3 = MetaReader.PATTERN_ISO_DATETIME_OFFSET;

        String[] date = new String[] {
                "1994-08-01",
                "2023-04-01",
                "1998-11-01",
        };

        String[] dateTime = new String[] {
                "2016-12-27T11:30:00Z",
                "2023-04-21T19:34:49Z",
                "2023-04-01T14:09:35Z",
                "2023-04-22T00:51:02.812104341Z",
        };

        String[] dateTimeWithOffset = new String[] {
                "2009-08-15T07:00:00+00:00",
                "2010-03-02T23:12:20.748000+00:00",
                "2023-04-01T11:20:38.883045+00:00",
        };

        String[] fake = new String[] {
                "1994-13-31",
                "2009-00-19",
                "2009d-00-19",
                "2012-31-12",
                "2023-04-01T24:09:35Z",
                "2009-08-15T07:00:00Z+00:00",
                "2009-08-15T07:00:00+00:00Z",
                "2009-08-15T29:59:59+59:59",
                "2023-04-22T00:51:02.oops04341Z",
                "2023-04-22t00:51:02.41Z",
        };

        for (int i = 0; i < date.length; i++) {
            boolean match = Pattern.matches(pattern1, date[i]);

            assertTrue(match, date[i] + " should match pattern \"" + pattern1 + "\"");
        }

        for (int i = 0; i < dateTime.length; i++) {
            boolean match = Pattern.matches(pattern2, dateTime[i]) && !Pattern.matches(pattern3, dateTime[i]);

            assertTrue(match, dateTime[i] + " should match pattern \"" + pattern2 + "\"");
        }

        for (int i = 0; i < dateTimeWithOffset.length; i++) {
            boolean match = Pattern.matches(pattern3, dateTimeWithOffset[i]) && !Pattern.matches(pattern2, dateTimeWithOffset[i]);

            assertTrue(match, dateTimeWithOffset[i] + " should match \"" + pattern3 + "\"");
        }

        for (int i = 0; i < fake.length; i++) {
            boolean match = Pattern.matches(pattern1, fake[i]) || Pattern.matches(pattern2, fake[i]) || Pattern.matches(pattern3, fake[i]);

            assertFalse(match, fake[i] + " should not match any date patterns");
        }
    }

    // TODO: write regex pattern for authors (low prio)
    @Test
    void testAuthorPattern() {
        String pattern = "";

        String[] authorNames = new String[] {
                "Test, Some",
        };

        assertTrue(true);
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
