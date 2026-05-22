import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Command-line interface for the contact directory. */
public final class ContactDirectoryCli {
    private ContactDirectoryCli() {
    }

    public static void run() {
        List<Contact> contacts = new ArrayList<>();
        try {
            contacts.addAll(ContactStorage.load());
        } catch (SQLException e) {
            System.err.println("Could not load contacts: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("\n  Welcome! Contacts are saved to contacts.db automatically.");

        while (true) {
            System.out.println("\n  Personal Contact Directory");
            System.out.println("  --------------------------");
            System.out.println("  1. View All Contacts");
            System.out.println("  2. Add New Contact");
            System.out.println("  3. Search Contacts");
            System.out.println("  4. Exit");
            System.out.print("  Choose an option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAll(contacts);
                case "2" -> addContact(contacts, scanner);
                case "3" -> search(contacts, scanner);
                case "4" -> {
                    System.out.println("\n  Goodbye! Your contacts are saved in the database.\n");
                    return;
                }
                default -> System.out.println("\n  Invalid choice. Please enter 1, 2, 3, or 4.\n");
            }
        }
    }

    private static void reload(List<Contact> contacts) throws SQLException {
        contacts.clear();
        contacts.addAll(ContactStorage.load());
    }

    private static void viewAll(List<Contact> contacts) {
        if (contacts.isEmpty()) {
            System.out.println("\n  No contacts saved yet.\n");
            return;
        }
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  ALL CONTACTS");
        System.out.println("=".repeat(50));
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            System.out.printf("%n  [%d] %s (%s)%n", i + 1, c.getName(), c.getGroup());
            System.out.printf("      Phone: %s%n", c.getPhone());
            System.out.printf("      Email: %s%n", c.getEmail());
        }
        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    private static void addContact(List<Contact> contacts, Scanner scanner) {
        System.out.println("\n--- Add New Contact ---");
        System.out.print("  Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("  Name cannot be empty. Contact not added.\n");
            return;
        }
        System.out.print("  Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("  Email: ");
        String email = scanner.nextLine().trim();

        String error = ContactValidator.validate(phone, email);
        if (error != null) {
            System.out.println("  " + error + "\n");
            return;
        }

        System.out.println("  Groups: Work, Family, Friends");
        System.out.print("  Group [" + ContactStorage.DEFAULT_GROUP + "]: ");
        String group = scanner.nextLine().trim();
        if (group.isEmpty()) {
            group = ContactStorage.DEFAULT_GROUP;
        }
        if (!ContactStorage.CONTACT_GROUPS.contains(group)) {
            group = ContactStorage.DEFAULT_GROUP;
        }

        try {
            ContactStorage.insert(new Contact(name, phone, email, group));
            reload(contacts);
            System.out.printf("%n  Contact '%s' added and saved.%n%n", name);
        } catch (SQLException e) {
            System.out.println("  Save failed: " + e.getMessage() + "\n");
        }
    }

    private static void search(List<Contact> contacts, Scanner scanner) {
        if (contacts.isEmpty()) {
            System.out.println("\n  No contacts to search.\n");
            return;
        }
        System.out.print("\n  Enter name to search: ");
        String query = scanner.nextLine().trim().toLowerCase();
        if (query.isEmpty()) {
            System.out.println("  Search term cannot be empty.\n");
            return;
        }

        List<Contact> matches = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.getName().toLowerCase().contains(query)) {
                matches.add(c);
            }
        }

        if (matches.isEmpty()) {
            System.out.printf("%n  No contact found matching '%s'.%n%n", query);
            return;
        }

        System.out.println("\n" + "-".repeat(50));
        System.out.println("  SEARCH RESULTS");
        System.out.println("-".repeat(50));
        for (Contact c : matches) {
            System.out.printf("%n  Name:  %s (%s)%n", c.getName(), c.getGroup());
            System.out.printf("  Phone: %s%n", c.getPhone());
            System.out.printf("  Email: %s%n", c.getEmail());
        }
        System.out.println("\n" + "-".repeat(50) + "\n");
    }
}
