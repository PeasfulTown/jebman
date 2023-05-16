package xyz.peasfultown;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.interfaces.JebmanGUI;
import xyz.peasfultown.interfaces.JebmanPrompt;
import xyz.peasfultown.interfaces.Prompter;

import java.io.IOException;
import java.sql.SQLException;

public class Application {
    private static boolean gui;
    private static MainController mc;

    static {
        gui = false;
    }

    public static void main(String[] args) {

        int argi = 0;
        while (argi < args.length) {
            String arg = args[argi];
            if (!arg.startsWith("-"))
                break;
            if (arg.length() < 2)
                usage();
            for (int i=1; i < arg.length(); i++) {
                char c = arg.charAt(i);
                switch (c) {
                    case 'g' :
                        gui = true;
                        break;
                    case 'p':
                        argi++;
                        setProgramMainPath(args[argi]);
                        break;
                    default :
                        usage();
                }
            }
            argi++;
        }

        try {
            mc = new MainController();
        } catch (SQLException e) {
            System.err.format("Unable to run database creation script: %s%n", e.getMessage());
            System.exit(-1);
        }

        if (gui) {
            JebmanGUI.start(mc);
        } else {
            JebmanPrompt prompt = new JebmanPrompt(new Prompter(System.in, System.out), mc);
            prompt.start();
        }
    }

    private static void usage() {
        System.err.println("jebman [-g]");
        System.exit(-1);
    }

    private static void setProgramMainPath(String path) {
        ApplicationConfig.setMainPath(path);
    }
}
