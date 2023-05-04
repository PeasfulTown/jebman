/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Tests for Publisher class.
 */
package xyz.peasfultown.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PublisherTest {

    @Test
    void testConstructor() {
        Publisher pub0 = new Publisher("Random House");
        Publisher pub1 = new Publisher("Vertigo");
        Publisher pub2 = new Publisher("FriesenPress");

        assertNotNull(pub0, "Object shouldn't be null after creation");
        assertNotNull(pub1, "Object shouldn't be null after creation");
        assertNotNull(pub2, "Object shouldn't be null after creation");

        assertTrue("Random House".equals(pub0.getName()));
        assertTrue("Vertigo".equals(pub1.getName()));
        assertTrue("FriesenPress".equals(pub2.getName()));
    }

    @Test
    void testEquals() {
        Publisher pub0 = new Publisher("Random House");
        Publisher pub1 = new Publisher("FriesenPress");
        Publisher pub2 = new Publisher("Vertigo");
        Publisher pub3 = new Publisher("FriesenPress");
        Publisher pub4 = new Publisher("Random House");

        assertTrue(pub1.equals(pub3), "Both publishers should be equal");
        assertFalse(pub0.equals(pub1), "Publishers should not be equal");
        assertTrue(pub0.equals(pub4), "Both publishers should be equal");
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
