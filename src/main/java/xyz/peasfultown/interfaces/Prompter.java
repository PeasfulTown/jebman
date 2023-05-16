package xyz.peasfultown.interfaces;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Prompter {
    private final Scanner scanner;
    private final PrintStream out;

    public Prompter(InputStream in, PrintStream out) {
        scanner = new Scanner(in);
        this.out = out;
    }

    public String promptForInput(String message) {
        out.print(message);
        return scanner.nextLine();
    }
}
