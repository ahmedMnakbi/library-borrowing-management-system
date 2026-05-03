package com.library.app;

import com.library.ui.ConsoleMenu;
import com.library.ui.LibrarySwingApp;

public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext();
        if (args.length > 0 && "--swing".equalsIgnoreCase(args[0])) {
            new LibrarySwingApp(applicationContext).launch();
            return;
        }
        new ConsoleMenu(applicationContext).start();
    }
}
