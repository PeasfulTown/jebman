package xyz.peasfultown.interfaces;

import xyz.peasfultown.ApplicationConfig;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Publisher;
import xyz.peasfultown.domain.Series;
import xyz.peasfultown.helpers.MetadataReaderException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static java.lang.System.out;
import static java.lang.System.err;

public class JebmanPrompt {
    private static final int DEFAULT_MAX_CHAR_LENGTH_NUMBER = 4;
    private static final int DEFAULT_MAX_CHAR_LENGTH = 25;
    // 4 spaces after
    private static final int DEFAULT_MAX_CHAR_LENGTH_DATE = 14;
    // 4 spaces after
    private static final int DEFAULT_MAX_CHAR_LENGTH_DATE_WITH_TIME = 23;
    private static char DELIM = '_';
    private static char TRUNCATE_SYMBOL = '~';
    private static MainController mc;
    private final Prompter prompt;
    private boolean continueProgram;

    public JebmanPrompt(Prompter prompt, MainController mc) {
        this.prompt = prompt;
        this.mc = mc;
    }

    public void start() {
        continueProgram = true;
        while (continueProgram) {
            prompt();
        }
    }

    private void usage() {
        // TODO: add list sort (date added, title, etc.)
        out.println("list                    List all books in store");
        out.println("add [path/to/ebook]     Add ebook to library");
        out.println("remove [id]             Remove book from library");
        out.println("info [id]               Print book information");
        out.println("quit/exit               Quit jebman");
    }

    private void prompt() {
        String input = prompt.promptForInput("[jebman]# ");
        continueProgram = processInput(input);
    }

    private boolean processInput(String input) {
        String[] parts = input.split(" ");

        switch (parts[0]) {
            case "list":
                out.println();
                list();
                break;
            case "add":
                if (!enoughArgs(parts))
                    break;
                add(parts[1]);
                break;
            case "remove":
                if (!enoughArgs(parts))
                    break;
                if (!isNumber(parts[1]))
                    break;
                remove(Integer.valueOf(parts[1]));
                break;
            case "info":
                if (!enoughArgs(parts))
                    break;
                if (!isNumber(parts[1]))
                    break;
                info(Integer.valueOf(parts[1]));
                break;
            case "quit":
            case "exit":
                return false;
            default:
                System.out.println("Unrecognized Command");
                usage();
                break;
        }

        return true;
    }

    private boolean isNumber(String num) {
        try {
            Integer.valueOf(num);
        } catch (NumberFormatException e) {
            System.out.println("Argument must be a number.");
            return false;
        }

        return true;
    }

    private boolean enoughArgs(String[] inp) {
        if (inp.length < 2 || inp[1].length() == 0) {
            System.out.println("Not enough arguments.");
            return false;
        }
        return true;
    }

    private void list() {
        Set<Book> books = mc.getBooks();
        for (Book b : books) {
            printBookItem(b);
        }
        out.println();
    }

    private void printBookItem(Book book) {
        // TODO: finish
        StringBuilder sb = new StringBuilder();

        appendPropertySpaces(sb, String.valueOf(book.getId()), DEFAULT_MAX_CHAR_LENGTH_NUMBER);
        appendPropertySpaces(sb, book.getTitle(), DEFAULT_MAX_CHAR_LENGTH);
        appendPropertySpaces(sb, mc.getBookAuthorByBookId(book.getId()).getName(), DEFAULT_MAX_CHAR_LENGTH);
        Series series = book.getSeries();
        appendPropertySpaces(sb, series != null ? series.getName() : "(No series)", DEFAULT_MAX_CHAR_LENGTH);
        appendPropertySpaces(sb, String.valueOf(book.getSeriesNumber()), DEFAULT_MAX_CHAR_LENGTH_NUMBER);
        Publisher publisher = book.getPublisher();
        appendPropertySpaces(sb, publisher != null ? publisher.getName() : "Unknown", DEFAULT_MAX_CHAR_LENGTH);
        appendPropertySpaces(sb, getStringFromTimeStamp(book.getPublishDate()), DEFAULT_MAX_CHAR_LENGTH_DATE);
        appendPropertySpaces(sb, getStringWithSecondsFromTimeStamp(book.getAddedDate()), DEFAULT_MAX_CHAR_LENGTH_DATE_WITH_TIME);
        appendPropertySpaces(sb, getStringWithSecondsFromTimeStamp(book.getModifiedDate()), DEFAULT_MAX_CHAR_LENGTH_DATE_WITH_TIME);

        System.out.println(sb);
    }

    private void appendPropertySpaces(StringBuilder sb, String strToAppend, int maxCharLength) {
        int charLength = 0;
        if (strToAppend != null) {
            sb.append(strToAppend);
            charLength = strToAppend.length();
        }

        if (maxCharLength == 0)
            maxCharLength = DEFAULT_MAX_CHAR_LENGTH;

        int diff = charLength - maxCharLength;
        while (diff > 0) {
            sb.deleteCharAt(sb.length() - 1);
            diff--;
        }

        if (diff == 0) {
            int ind = sb.length();
            sb.replace(ind - 2, ind, String.valueOf(TRUNCATE_SYMBOL) + DELIM);
        }

        while (charLength < maxCharLength) {
            sb.append(DELIM);
            charLength++;
        }
    }

    private String getStringFromTimeStamp(Instant time) {
        return LocalDate.ofInstant(time, ZoneId.systemDefault()).toString();
    }

    private String getStringWithSecondsFromTimeStamp(Instant time) {
        return getStringFromTimeStamp(time) + "@" +
                LocalTime.ofInstant(time, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private void add(String path) {
        try {
            mc.insertBook(path);
        } catch (DAOException e) {
            err.format("Failed to create record of book in database: %s%n", e.getMessage());
        } catch (IOException e) {
            err.format("Failed to add book to jebman library: %s%n", e.getMessage());
        } catch (MetadataReaderException e) {
            err.format("Failed to read book metadata: %s%n", e.getMessage());
        }
        out.println("Book added to jebman!");
    }

    private void remove(int id) {
        try {
            mc.removeBook(id);
        } catch (DAOException e) {
            out.format("Unable to remove book book from database: %s%n", e.getMessage());
        } catch (IOException e) {
            out.format("Unable to remove book from filesystem: %s%n", e.getMessage());
        }
    }

    private void info(int id) {
        System.out.println("print file info!");
        System.out.println(ApplicationConfig.MAIN_PATH);
    }

}
