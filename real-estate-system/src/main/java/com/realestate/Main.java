package com.realestate;

import com.realestate.ui.AdminConsole;
import java.util.Scanner;

/**
 * Main entry point for the Real Estate Management System.
 * Provides initial system setup and mode selection.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        displayWelcomeMessage();
        int mode = selectMode();
        
        switch (mode) {
            case 1:
                // Start Admin Console
                AdminConsole adminConsole = new AdminConsole();
                adminConsole.start();
                break;
            case 2:
                // Customer mode - to be implemented
                System.out.println("Customer mode is under development.");
                break;
            case 3:
                // Guest mode - to be implemented
                System.out.println("Guest mode is under development.");
                break;
            default:
                System.out.println("Invalid mode selected. Exiting...");
        }
        
        scanner.close();
    }

    private static void displayWelcomeMessage() {
        System.out.println("================================================");
        System.out.println("Welcome to the Real Estate Management System");
        System.out.println("Version 1.0");
        System.out.println("================================================");
    }

    private static int selectMode() {
        int choice = 0;
        boolean validInput = false;

        do {
            try {
                System.out.println("\nPlease select your mode:");
                System.out.println("1. Administrator");
                System.out.println("2. Customer");
                System.out.println("3. Guest");
                System.out.print("\nEnter your choice (1-3): ");

                choice = Integer.parseInt(scanner.nextLine());
                validInput = (choice >= 1 && choice <= 3);

                if (!validInput) {
                    System.out.println("Please enter a number between 1 and 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        } while (!validInput);

        return choice;
    }
}
