/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: Author object representation.
 */
package xyz.peasfultown.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Author implements Record {
    private int id;
    private String name;

    public Author() {

    }
    public Author(String name) {
        this.name = name;
    }

    public Author(int id, String name) {
        this(name);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Author[] getArrayOfAuthorsObjectsFromString(String authors) {
        String[] strArr = authors.split(",");
        Author[] auArr = new Author[strArr.length];
        int i = 0;
        while (i < strArr.length) {
            auArr[i] = new Author(strArr[i].trim());
            i++;
        }

        return auArr;
    }

    public static List<Author> getListOfAuthorsObjectsFromString(String authors) {
        String[] strArr = authors.split(",");
        List<Author> auList = new ArrayList<>();
        int i = 0;
        while (i < strArr.length) {
            auList.add(new Author(strArr[i].trim()));
            i++;
        }

        return auList;
    }

    public static Author parse(String author) {
        String[] parts = author.split(",");
        return new Author(Integer.valueOf(parts[0]), parts[1]);
    }
    public static String getStringOfAuthorFromList(List<Author> authors) {
        StringJoiner sj = new StringJoiner(",");
        int i = 0;
        while (i < authors.size()) {
            sj.add(authors.get(i).getName());
            i++;
        }

        return sj.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        Author author = (Author) obj;

        return new EqualsBuilder()
                .append(this.getId(), author.getId())
                .append(this.getName(), author.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 15)
                .append(this.id)
                .append(this.name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(",")
                .add(String.valueOf(getId()))
                .add(getName()).toString();
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
