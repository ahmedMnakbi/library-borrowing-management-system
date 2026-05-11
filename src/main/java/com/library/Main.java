package com.library;

import com.library.service.LibraryService;
import com.library.ui.ConsoleMenu;

public class Main {
    public static void main(String[] args) {
        LibraryService service = new LibraryService();
        ConsoleMenu menu = new ConsoleMenu(service);
        menu.start();
    }
}
