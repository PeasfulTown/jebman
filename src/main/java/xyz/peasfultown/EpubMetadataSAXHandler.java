/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: SAXHandler implementation for reading Epub content.opf files for its metadata.
 */
package xyz.peasfultown;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

class EpubMetadataSAXHandler extends DefaultHandler {
    private HashMap<String, String> meta;
    private StringBuilder elementValue;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        elementValue.append(ch, start, length);
    }

    @Override
    public void startDocument() throws SAXException {
        this.meta = new HashMap<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.elementValue = new StringBuilder();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = this.elementValue.toString().trim();

        // Pick data
        switch (qName) {
            case "dc:identifier":
                this.meta.putIfAbsent("identifier", value);
                break;
            case "dc:title":
                this.meta.putIfAbsent("title", value);
                break;
            case "dc:description":
                this.meta.putIfAbsent("description", value);
                break;
            case "dc:publisher":
                this.meta.putIfAbsent("publisher", value);
                break;
            case "dc:creator":
                this.meta.putIfAbsent("author", value);
                break;
            case "dc:date":
                this.meta.putIfAbsent("date", value);
                break;
            default:
                break;
        }

        if (localName.equals("metadata")) {
            endDocument();
        }

    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    public HashMap<String, String> getResults() {
        return this.meta;
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
