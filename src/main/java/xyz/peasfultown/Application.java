package xyz.peasfultown;

import xyz.peasfultown.interfaces.JebmanGUI;

public class Application {

    public static void main(String[] args) {
        boolean tui = true;
        boolean gui = false;

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
                        tui = false;
                        gui = true;
                        break;
                    default :
                        usage();
                }
            }
            argi++;
        }

        if (gui) {
            JebmanGUI.main(args);
        }
    }

    private static void usage() {
        System.err.println("jebman [-g]");
        System.exit(-1);
    }
}
