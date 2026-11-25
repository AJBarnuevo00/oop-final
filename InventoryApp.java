import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * InventoryApp (main class)
 *
 * Menu, Scanner input, and input-validation helpers only.
 * It calls Inventory methods and prints the results. The business rules
 * live in Product, its subclasses, and Inventory.
 */
public class InventoryApp {

    private static final Scanner sc = new Scanner(System.in);
    private static Inventory inventory;

    // Upper limit for typed quantities, so absurd numbers are rejected
    private static final int MAX_AMOUNT = 100000;

    // MAIN================================================================
    public static void main(String[] args) {
        inventory = new Inventory();   // loads the sample data

        int choice = -1;
        do {
            printMenu();
            try {
                choice = readInt(sc, "Choose an option: ", 0, 11);
                switch (choice) {
                    case 1:  addProductFlow();  break;
                    case 2:  viewAllFlow();     break;
                    case 3:  searchFlow();      break;
                    case 4:  updateFlow();      break;
                    case 5:  deleteFlow();      break;
                    case 6:  stockInFlow();     break;
                    case 7:  stockOutFlow();    break;
                    case 8:  lowStockFlow();    break;
                    case 9:  sortFlow();        break;
                    case 10: summaryFlow();     break;
                    case 11: historyFlow();     break;
                    case 0:  System.out.println("Goodbye!"); break;
                }
            } catch (IllegalArgumentException e) {
                // Rule violations thrown by Inventory / Product
                System.out.println("Error: " + e.getMessage());
            } catch (NoSuchElementException e) {
                // Input stream closed (for example Ctrl+D): leave instead of looping forever
                System.out.println("\nInput ended. Exiting.");
                choice = 0;
            } catch (RuntimeException e) {
                // Safety net so the program never crashes on the menu
                System.out.println("Unexpected error: " + e.getMessage());
            }
        } while (choice != 0);

        sc.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=========== PET SHOP INVENTORY ===========");
        System.out.println(" 1. Add product");
        System.out.println(" 2. View all products");
        System.out.println(" 3. Search product");
        System.out.println(" 4. Update product");
        System.out.println(" 5. Delete product");
        System.out.println(" 6. Stock in");
        System.out.println(" 7. Stock out");
        System.out.println(" 8. Low-stock alerts");
        System.out.println(" 9. Sort products");
        System.out.println("10. Summary report");
        System.out.println("11. Transaction history");
        System.out.println(" 0. Exit");
        System.out.println("==========================================");
    }

    // MENU FLOWS=============================================
    private static void addProductFlow() {
        System.out.println("\n--- Add Product ---");
        System.out.println("Category:");
        System.out.println("  1. Pet Food");
        System.out.println("  2. Pet Toy");
        System.out.println("  3. Pet Medicine");
        System.out.println("  4. Pet Accessory");
        int category = readInt(sc, "Choose a category (1-4): ", 1, 4);

        // Ask for the ID first so a duplicate is caught before the other details
        String id;
        while (true) {
            id = readNonEmptyString(sc, "Product ID: ");
            if (inventory.findById(id) != null) {
                System.out.println("Error: a product with ID '" + id + "' already exists. Try another ID.");
            } else {
                break;
            }
        }

        String name = readNonEmptyString(sc, "Name: ");
        double price = readDouble(sc, "Unit price: ");
        int quantity = readInt(sc, "Starting quantity: ", 0, MAX_AMOUNT);
        int reorderLevel = readInt(sc, "Reorder level: ", 0, MAX_AMOUNT);

        // NOTE: the Product constructors take (name, id, ...) in that order.
        Product product;
        switch (category) {
            case 1:
                String animal = readNonEmptyString(sc, "Target animal (Dog, Cat, Fish, Bird): ");
                double weightKg = readPositiveDouble(sc, "Weight in kg: ");
                product = new PetFood(name, id, price, quantity, reorderLevel, animal, weightKg);
                break;
            case 2:
                int durability = readInt(sc, "Durability (1 = lowest, 5 = highest): ", 1, 5);
                product = new PetToy(name, id, price, quantity, reorderLevel, durability);
                break;
            case 3:
                double dosageMg = readPositiveDouble(sc, "Dosage in mg: ");
                String expiry = readDate(sc, "Expiration date (yyyy-MM-dd): ");
                product = new PetMedicine(name, id, price, quantity, reorderLevel, dosageMg, expiry);
                break;
            default:
                String material = readNonEmptyString(sc, "Material (for example nylon, steel, plastic): ");
                product = new PetAccessory(name, id, price, quantity, reorderLevel, material);
                break;
        }

        inventory.addProduct(product);   // throws if the inventory is full or the ID is a duplicate
        System.out.println("Product added successfully:");
        printProducts(new Product[] { product });
    }

    private static void viewAllFlow() {
        System.out.println("\n--- All Products ---");
        printProducts(inventory.getAllProducts());
    }

    private static void searchFlow() {
        System.out.println("\n--- Search Product ---");
        System.out.println("  1. Search by ID (exact)");
        System.out.println("  2. Search by name (partial, any letter case)");
        int option = readInt(sc, "Choose (1-2): ", 1, 2);

        if (option == 1) {
            String id = readNonEmptyString(sc, "Enter product ID: ");
            Product product = inventory.findById(id);
            if (product == null) {
                System.out.println("No product found with ID '" + id + "'.");
            } else {
                Product[] result = { product };
                printProducts(result);
                printHandlingNotes(result);
            }
        } else {
            String keyword = readNonEmptyString(sc, "Enter part of the name: ");
            Product[] matches = inventory.searchByName(keyword);
            if (matches.length == 0) {
                System.out.println("No products found with a name containing '" + keyword + "'.");
            } else {
                printProducts(matches);
                printHandlingNotes(matches);
            }
        }
    }

    private static void updateFlow() {
        System.out.println("\n--- Update Product ---");
        String id = readNonEmptyString(sc, "Enter the ID of the product to update: ");
        Product product = inventory.findById(id);
        if (product == null) {
            System.out.println("No product found with ID '" + id + "'.");
            return;
        }

        System.out.println("Current record:");
        printProducts(new Product[] { product });
        System.out.println("Press Enter to keep a value unchanged.");

        String name = readStringOrKeep("New name", product.getName());
        double price = readDoubleOrKeep("New price", product.getUnitPrice());
        int reorderLevel = readIntOrKeep("New reorder level", product.getReorderLevel());

        // Category-specific field. The array is {fieldName, newValue}.
        System.out.println("Category-specific field you can change:");
        System.out.println("  food: targetAnimal, weightKg | medicine: dosageMg, expirationDate");
        System.out.println("  toy: durability | accessory: material");
        System.out.println("  Current details: " + product.getDetails());
        System.out.print("Field name to change (press Enter to skip): ");
        String fieldName = sc.nextLine().trim();

        String[] specificField = null;   // null = leave the category field unchanged
        if (!fieldName.isEmpty()) {
            String value = readNonEmptyString(sc, "New value for " + fieldName + ": ");
            specificField = new String[] { fieldName, value };
        }

        Product updated = inventory.updateProduct(product.getId(), name, price, reorderLevel, specificField);
        System.out.println("Updated record:");
        printProducts(new Product[] { updated });
    }

    private static void deleteFlow() {
        System.out.println("\n--- Delete Product ---");
        String id = readNonEmptyString(sc, "Enter the ID of the product to delete: ");
        Product product = inventory.findById(id);
        if (product == null) {
            System.out.println("No product found with ID '" + id + "'.");
            return;
        }

        printProducts(new Product[] { product });
        boolean confirmed = readYesNo(sc, "Are you sure you want to delete this product? (Y/N): ");
        if (!confirmed) {
            System.out.println("Delete cancelled. Nothing was removed.");
            return;
        }

        if (inventory.deleteProduct(product.getId())) {
            System.out.println("Product '" + product.getName() + "' was deleted.");
        } else {
            System.out.println("The product could not be deleted.");
        }
    }

    private static void stockInFlow() {
        System.out.println("\n--- Stock In (Restock) ---");
        String id = readNonEmptyString(sc, "Enter product ID: ");
        Product product = inventory.findById(id);
        if (product == null) {
            System.out.println("No product found with ID '" + id + "'.");
            return;
        }

        System.out.println(product.getName() + " - current quantity: " + product.getQuantity());
        int amount = readInt(sc, "Quantity to add: ", 1, MAX_AMOUNT);

        inventory.stockIn(product.getId(), amount);
        System.out.println("Added " + amount + " unit(s). New quantity: " + product.getQuantity());
    }

    private static void stockOutFlow() {
        System.out.println("\n--- Stock Out (Sale or Issue) ---");
        String id = readNonEmptyString(sc, "Enter product ID: ");
        Product product = inventory.findById(id);
        if (product == null) {
            System.out.println("No product found with ID '" + id + "'.");
            return;
        }

        System.out.println(product.getName() + " - available quantity: " + product.getQuantity());
        if (product.getQuantity() == 0) {
            System.out.println("Refused: this product is out of stock.");
            return;
        }

        int amount = readInt(sc, "Quantity to remove: ", 1, MAX_AMOUNT);

        // Safety check so a too-large request is refused with a clear message.
        if (amount > product.getQuantity()) {
            System.out.println("Refused: cannot remove " + amount + " unit(s). Only "
                    + product.getQuantity() + " available.");
            return;
        }

        boolean nowLow = inventory.stockOut(product.getId(), amount);
        System.out.println("Removed " + amount + " unit(s). New quantity: " + product.getQuantity());

        if (nowLow) {
            System.out.println("*** LOW STOCK WARNING: " + product.getName()
                    + " is at " + product.getQuantity()
                    + " (reorder level: " + product.getReorderLevel() + "). Please restock. ***");
        }
    }

    private static void lowStockFlow() {
        System.out.println("\n--- Low-Stock Alerts ---");
        Product[] low = inventory.getLowStockProducts();
        if (low.length == 0) {
            System.out.println("Good news: no products are at or below their reorder level.");
        } else {
            System.out.println("These products are at or below their reorder level:");
            printProducts(low);
        }
    }

    private static void sortFlow() {
        System.out.println("\n--- Sort Products ---");
        System.out.println("  1. By name");
        System.out.println("  2. By price");
        System.out.println("  3. By quantity");
        int field = readInt(sc, "Sort by (1-3): ", 1, 3);

        System.out.println("  1. Ascending");
        System.out.println("  2. Descending");
        int direction = readInt(sc, "Order (1-2): ", 1, 2);

        int mode;
        switch (field) {
            case 1:  mode = Inventory.SORT_BY_NAME;     break;
            case 2:  mode = Inventory.SORT_BY_PRICE;    break;
            default: mode = Inventory.SORT_BY_QUANTITY; break;
        }

        Product[] sorted = inventory.sortProducts(mode, direction == 1);
        printProducts(sorted);
    }

    private static void summaryFlow() {
        System.out.println("\n--- Summary Report ---");
        System.out.println("Total products        : " + inventory.getTotalProducts());
        System.out.println("Total units           : " + inventory.getTotalUnits());
        System.out.printf("Total inventory value : %.2f%n", inventory.getTotalValue());

        System.out.println();
        System.out.printf("%-20s %9s %15s%n", "Category", "Products", "Value");
        System.out.println("-------------------------------------------------");
        for (String category : inventory.getCategoryNames()) {
            System.out.printf("%-20s %9d %15.2f%n",
                    category,
                    inventory.countByCategory(category),
                    inventory.valueByCategory(category));
        }

        System.out.println();
        System.out.println("Low-stock products:");
        Product[] low = inventory.getLowStockProducts();
        if (low.length == 0) {
            System.out.println("None.");
        } else {
            printProducts(low);
        }
    }

    private static void historyFlow() {
        System.out.println("\n--- Transaction History ---");
        StockTransaction[] history = inventory.getTransactions();
        if (history.length == 0) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        for (StockTransaction transaction : history) {
            System.out.println(transaction);   // uses StockTransaction.toString()
        }
        System.out.println(history.length + " transaction(s)");
    }


    // DISPLAY HELPERS================================================================
    //prints an aligned table. Details come from each product's own getDetails()
    private static void printProducts(Product[] list) {
        if (list == null || list.length == 0) {
            System.out.println("No products to display.");
            return;
        }

        printLine();
        System.out.printf("%-10s %-24s %-14s %10s %6s %8s  %s%n",
                "ID", "Name", "Category", "Price", "Qty", "Reorder", "Details");
        printLine();
        for (Product p : list) {
            String flag = p.isLowStock() ? "  [LOW STOCK]" : "";
            System.out.printf("%-10.10s %-24.24s %-14.14s %10.2f %6d %8d  %s%n",
                    p.getId(), p.getName(), p.getCategory(),
                    p.getUnitPrice(), p.getQuantity(), p.getReorderLevel(),
                    p.getDetails() + flag);
        }
        printLine();
        System.out.println(list.length + " product(s)");
    }

    private static void printHandlingNotes(Product[] list) {
        System.out.println("Handling notes:");
        for (Product p : list) {
            System.out.println("  " + p.getName() + ": " + p.getHandlingNote());
        }
    }

    private static void printLine() {
        System.out.println("----------------------------------------------------------------------------------------------------");
    }

    // INPUT HELPERS (each one keeps asking until the input is valid)==========================================
    //whole number between min and max (inclusive)
    private static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println("Error: enter a number from " + min + " to " + max + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a whole number.");
            }
        }
    }

    //decimal number that is 0 or higher
    private static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
                    System.out.println("Error: enter a number that is 0 or higher.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a valid number.");
            }
        }
    }

    //decimal number greater than zero (for weight and dosage)
    private static double readPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            double value = readDouble(sc, prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Error: the value must be greater than zero.");
        }
    }

    //Text that is not empty
    private static String readNonEmptyString(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Error: this cannot be empty.");
        }
    }

    //Y or N. Returns true for Y
    private static boolean readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("Y") || line.equalsIgnoreCase("YES")) {
                return true;
            }
            if (line.equalsIgnoreCase("N") || line.equalsIgnoreCase("NO")) {
                return false;
            }
            System.out.println("Error: please enter Y or N.");
        }
    }

    // real calendar date in yyyy-MM-dd format, returned as text
    private static String readDate(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                LocalDate.parse(line);   // throws if it is not a valid yyyy-MM-dd date
                return line;
            } catch (DateTimeParseException e) {
                System.out.println("Error: enter a valid date like 2027-06-30.");
            }
        }
    }

    //press Enter to keep the current value, versions, used by Update
    private static String readStringOrKeep(String prompt, String current) {
        System.out.print(prompt + " [" + current + "]: ");
        String line = sc.nextLine().trim();
        return line.isEmpty() ? current : line;
    }

    private static double readDoubleOrKeep(String prompt, double current) {
        while (true) {
            System.out.print(prompt + " [" + String.format("%.2f", current) + "]: ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                return current;
            }
            try {
                double value = Double.parseDouble(line);
                if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
                    System.out.println("Error: enter a number that is 0 or higher.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a valid number.");
            }
        }
    }

    private static int readIntOrKeep(String prompt, int current) {
        while (true) {
            System.out.print(prompt + " [" + current + "]: ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                return current;
            }
            try {
                int value = Integer.parseInt(line);
                if (value < 0 || value > MAX_AMOUNT) {
                    System.out.println("Error: enter a number from 0 to " + MAX_AMOUNT + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a whole number.");
            }
        }
    }
}
