package xyz.peasfultown;

import xyz.peasfultown.interfaces.JebmanGUI;
import xyz.peasfultown.interfaces.JebmanPrompt;

public class Application {

    public static void main(String[] args) {
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
        } else {
            JebmanPrompt.main(args);
        }
    }

    private static void usage() {
        System.err.println("jebman [-g]");
        System.exit(-1);
    }
}
