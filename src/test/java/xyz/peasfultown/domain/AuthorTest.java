/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Tests for Author class.
 */
package xyz.peasfultown.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


public class AuthorTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthorTest.class);

    @BeforeAll
    static void setUp() {
        logger.info("Setting up test for Author class");
    }

    @Test
    public void testAuthorNameCorrect() {
        logger.info("Executing test for author name field");
        Author author = new Author("Joe Abercrombie");
        assertEquals("Joe Abercrombie", author.getName(), "Failure, initialized author name incorrect");
        assertNotNull(author.getName(), "Author name is null when it's not supposed to be null.");
    }

    @Test
    public void testEqualsAuthor() {
        logger.info("Executing test for equals method");
        Author author = new Author("Joe Abercrombie");
        Author newAuthor = new Author("Joe Abercrombie");
        Author differentAuthor = new Author("Joshua Bloch");

        assertTrue(author.equals(newAuthor), "Author of same name not equal to author when they should be equal.");
        assertFalse(author.equals(differentAuthor), "Author of a different name should not equal to current author.");
    }

    @Test
    public void getArrayOfAuthorsObjsFromString() {
        logger.info("Executing test for getting array of authors objects from a string object");
        Author[] auArr = new Author[]{
                new Author("Joshua Bloch"),
                new Author("Joe Abercrombie"),
                new Author("Jane Austen"),
                new Author("Neil Gaiman"),
        };

        // Author names should be separated by commas
        String auStr1 = "Joshua Bloch, Joe Abercrombie, Jane Austen, Neil Gaiman";
        String auStr2 = "Joshua Bloch,Joe Abercrombie,Jane Austen,Neil Gaiman";

        assertTrue(Arrays.equals(Author.getArrayOfAuthorsObjectsFromString(auStr1), auArr));
        assertTrue(Arrays.equals(Author.getArrayOfAuthorsObjectsFromString(auStr2), auArr));
    }

    @Test
    void getListOfAuthorObjectsFromString() {
        logger.info("Executing test for getting list of author from string");

        ArrayList<Author> auList = new ArrayList<>();
        auList.add(new Author("Joshua Bloch"));
        auList.add(new Author("Joe Abercrombie"));
        auList.add(new Author("Jane Austen"));
        auList.add(new Author("Neil Gaiman"));

        String auStr1 = "Joshua Bloch, Joe Abercrombie, Jane Austen, Neil Gaiman";
        String auStr2 = "Joshua Bloch,Joe Abercrombie,Jane Austen,Neil Gaiman";

        assertTrue(auList.equals(Author.getListOfAuthorsObjectsFromString(auStr1)));
        assertTrue(auList.equals(Author.getListOfAuthorsObjectsFromString(auStr2)));
    }

    @Test
    void getStringOfAuthorsFromListOfAuthorObjects() {
        logger.info("Executing test for getting string of authors from list of authors");

        ArrayList<Author> auList = new ArrayList<>();
        auList.add(new Author("Joshua Bloch"));
        auList.add(new Author("Joe Abercrombie"));
        auList.add(new Author("Jane Austen"));
        auList.add(new Author("Neil Gaiman"));

        String auStr1 = "Joshua Bloch,Joe Abercrombie,Jane Austen,Neil Gaiman";

        String genStr = Author.getStringOfAuthorFromList(auList);
        logger.info("String of author from list: {}", genStr);
        assertTrue(auStr1.equals(Author.getStringOfAuthorFromList(auList)));
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
