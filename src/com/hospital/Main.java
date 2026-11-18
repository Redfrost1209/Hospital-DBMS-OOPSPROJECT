package com.hospital;

import com.hospital.config.DatabaseManager;
import com.hospital.ui.ConsoleMenu;

/**
 * Hospital Management System Entry Point.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Eagerly initialize SQLite RDBMS and execute schema & seed migration
            DatabaseManager.getInstance();

            // Launch interactive Console UI
            ConsoleMenu menu = new ConsoleMenu();
            menu.start();
        } catch (Exception e) {
            System.err.println("Fatal application error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
