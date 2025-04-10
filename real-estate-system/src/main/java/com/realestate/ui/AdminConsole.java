package com.realestate.ui;

import com.realestate.models.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line interface for system administrators.
 * Demonstrates Scanner usage, control flow, and operators.
 */
public class AdminConsole {
    private final Scanner scanner;
    private AdminUser currentAdmin;
    private final List<Property> properties;
    private boolean isRunning;
    
    // Property counter for demonstration
    private int propertyCount;

    public AdminConsole() {
        this.scanner = new Scanner(System.in);
        this.properties = new ArrayList<>();
        this.propertyCount = 0;
        this.isRunning = false;
    }

    public void start() {
        isRunning = true;
        login();
        
        while (isRunning) {
            displayMenu();
            int choice = getMenuChoice();
            processMenuChoice(choice);
        }
        
        scanner.close();
    }

    private void login() {
        System.out.println("=== Admin Login ===");
        
        System.out.print("Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        // For demonstration, create a default admin
        // In real system, this would validate against database
        currentAdmin = new AdminUser(
            "Admin",
            "User",
            email,
            "1234567890",
            password, // In real system, this would be hashed
            "Sales",
            "Manager",
            true
        );
        
        System.out.println("\nWelcome, " + currentAdmin.getFullName() + "!");
    }

    private void displayMenu() {
        System.out.println("\n=== Real Estate Management System ===");
        System.out.println("1. Add New Property");
        System.out.println("2. List All Properties");
        System.out.println("3. Search Properties");
        System.out.println("4. Update Property");
        System.out.println("5. Delete Property");
        System.out.println("6. View System Statistics");
        System.out.println("7. Logout");
        System.out.println("================================");
    }

    private int getMenuChoice() {
        int choice = 0;
        boolean validInput = false;
        
        do {
            try {
                System.out.print("Enter your choice (1-7): ");
                choice = Integer.parseInt(scanner.nextLine());
                validInput = (choice >= 1 && choice <= 7);
                
                if (!validInput) {
                    System.out.println("Please enter a number between 1 and 7.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        } while (!validInput);
        
        return choice;
    }

    private void processMenuChoice(int choice) {
        switch (choice) {
            case 1:
                addNewProperty();
                break;
            case 2:
                listProperties();
                break;
            case 3:
                searchProperties();
                break;
            case 4:
                updateProperty();
                break;
            case 5:
                deleteProperty();
                break;
            case 6:
                viewStatistics();
                break;
            case 7:
                logout();
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private void addNewProperty() {
        System.out.println("\n=== Add New Property ===");
        
        // Demonstrate property type selection using if/else
        System.out.println("Select property type:");
        System.out.println("1. Residential");
        System.out.println("2. Commercial");
        
        int typeChoice = Integer.parseInt(scanner.nextLine());
        
        // Basic property details
        System.out.print("Title: ");
        String title = scanner.nextLine();
        
        System.out.print("Description: ");
        String description = scanner.nextLine();
        
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine());
        
        System.out.print("Area (sq ft): ");
        double area = Double.parseDouble(scanner.nextLine());
        
        System.out.print("Address: ");
        String address = scanner.nextLine();
        
        System.out.print("City: ");
        String city = scanner.nextLine();
        
        System.out.print("State: ");
        String state = scanner.nextLine();
        
        System.out.print("Zip Code: ");
        String zipCode = scanner.nextLine();

        Property newProperty;
        
        if (typeChoice == 1) {
            System.out.print("Number of Bedrooms: ");
            int bedrooms = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Number of Bathrooms: ");
            int bathrooms = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Has Garage (true/false): ");
            boolean hasGarage = Boolean.parseBoolean(scanner.nextLine());
            
            System.out.print("Has Garden (true/false): ");
            boolean hasGarden = Boolean.parseBoolean(scanner.nextLine());
            
            System.out.print("Property Style: ");
            String style = scanner.nextLine();
            
            newProperty = new ResidentialProperty(
                title, description, price, area, address, city, state, zipCode,
                bedrooms, bathrooms, hasGarage, hasGarden, style
            );
        } else {
            System.out.print("Property Use (Office/Retail/Industrial): ");
            String propertyUse = scanner.nextLine();
            
            System.out.print("Number of Units: ");
            int units = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Has Parking (true/false): ");
            boolean hasParking = Boolean.parseBoolean(scanner.nextLine());
            
            System.out.print("Number of Parking Spaces: ");
            int parkingSpaces = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Zoning: ");
            String zoning = scanner.nextLine();
            
            newProperty = new CommercialProperty(
                title, description, price, area, address, city, state, zipCode,
                propertyUse, units, hasParking, parkingSpaces, zoning
            );
        }
        
        properties.add(newProperty);
        propertyCount++; // Demonstrate unary operator
        
        System.out.println("\nProperty added successfully!");
    }

    private void listProperties() {
        System.out.println("\n=== Property Listings ===");
        
        if (properties.isEmpty()) {
            System.out.println("No properties found.");
            return;
        }
        
        for (Property property : properties) {
            System.out.println("\n" + property.toString());
        }
    }

    private void searchProperties() {
        System.out.println("\n=== Search Properties ===");
        
        System.out.print("Enter minimum price: ");
        double minPrice = Double.parseDouble(scanner.nextLine());
        
        System.out.print("Enter maximum price: ");
        double maxPrice = Double.parseDouble(scanner.nextLine());
        
        System.out.println("\nMatching properties:");
        
        // Demonstrate logical operators in search
        for (Property property : properties) {
            if (property.getPrice() >= minPrice && property.getPrice() <= maxPrice) {
                System.out.println(property.toString());
            }
        }
    }

    private void updateProperty() {
        System.out.println("\n=== Update Property ===");
        
        if (properties.isEmpty()) {
            System.out.println("No properties to update.");
            return;
        }
        
        // List properties for selection
        for (int i = 0; i < properties.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, properties.get(i).getTitle());
        }
        
        System.out.print("\nSelect property number to update: ");
        int index = Integer.parseInt(scanner.nextLine()) - 1;
        
        if (index >= 0 && index < properties.size()) {
            Property property = properties.get(index);
            
            System.out.print("New price (current: " + property.getPrice() + "): ");
            double newPrice = Double.parseDouble(scanner.nextLine());
            property.setPrice(newPrice);
            
            System.out.println("Property updated successfully!");
        } else {
            System.out.println("Invalid property number!");
        }
    }

    private void deleteProperty() {
        System.out.println("\n=== Delete Property ===");
        
        if (properties.isEmpty()) {
            System.out.println("No properties to delete.");
            return;
        }
        
        // List properties for selection
        for (int i = 0; i < properties.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, properties.get(i).getTitle());
        }
        
        System.out.print("\nSelect property number to delete: ");
        int index = Integer.parseInt(scanner.nextLine()) - 1;
        
        if (index >= 0 && index < properties.size()) {
            Property removed = properties.remove(index);
            propertyCount--; // Demonstrate unary operator
            System.out.println("Deleted: " + removed.getTitle());
        } else {
            System.out.println("Invalid property number!");
        }
    }

    private void viewStatistics() {
        System.out.println("\n=== System Statistics ===");
        
        // Demonstrate various operators
        int residentialCount = 0;
        int commercialCount = 0;
        double totalValue = 0.0;
        
        for (Property property : properties) {
            if (property instanceof ResidentialProperty) {
                residentialCount++;
            } else {
                commercialCount++;
            }
            totalValue += property.getPrice();
        }
        
        // Using ternary operator for average calculation
        double averageValue = propertyCount > 0 ? totalValue / propertyCount : 0;
        
        System.out.println("Total Properties: " + propertyCount);
        System.out.println("Residential Properties: " + residentialCount);
        System.out.println("Commercial Properties: " + commercialCount);
        System.out.printf("Total Property Value: $%.2f%n", totalValue);
        System.out.printf("Average Property Value: $%.2f%n", averageValue);
    }

    private void logout() {
        System.out.println("\nLogging out...");
        System.out.println("Thank you for using the Real Estate Management System!");
        isRunning = false;
    }

    public static void main(String[] args) {
        AdminConsole console = new AdminConsole();
        console.start();
    }
}
