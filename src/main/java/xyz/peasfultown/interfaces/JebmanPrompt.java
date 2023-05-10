package xyz.peasfultown.interfaces;

import java.util.Scanner;

public class JebmanPrompt {
    private static final Scanner scanner;

    static {
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        while (true) {
            prompt();
        }
    }

    public static void usage() {
        // TODO: add list sort (date added, title, etc.)
        System.out.println("list                    List all books in store");
        System.out.println("add [path/to/ebook]     Add ebook to library");
        System.out.println("remove [id]             Remove book from library");
        System.out.println("info [id]               Print book information");
        System.out.println("quit                    Quit jebman");
        System.out.println("exit                    Quit jebman");
    }

    private static void prompt() {
        System.out.print("[jebman]# ");
        String input = scanner.nextLine();

        processInput(input);
    }

    private static boolean processInput(String input) {
        String[] parts = input.split(" ");

        switch (parts[0]) {
            case "list":
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
                System.exit(0);
            default:
                System.out.println("Unrecognized Command");
                usage();
                break;
        }

        return true;
    }

    private static boolean isNumber(String num) {
        try {
            Integer.valueOf(num);
        } catch (NumberFormatException e) {
            System.out.println("Argument must be a number");
            return false;
        }

        return true;
    }

    private static boolean enoughArgs(String[] inp) {
        if (inp.length < 2 || inp[1].length() == 0) {
            System.out.println("Not enough arguments");
            return false;
        }
        return true;
    }

    private static void list() {
        System.out.println("list files!");
    }

    private static void add(String path) {
        System.out.println("add file!");
    }

    private static void remove(int id) {
        System.out.println("remove file!");
    }

    private static void info(int id) {
        System.out.println("print file info!");
    }

}
