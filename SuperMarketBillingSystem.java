import java.io.*;
import java.util.*;

class User {
    String email, password, role;
    double credit = 1000;
    int loyaltyPoints = 0;

    User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User fromString(String line) {
        String[] parts = line.split(",");
        User u = new User(parts[0], parts[1], parts[2]);
        u.credit = Double.parseDouble(parts[3]);
        u.loyaltyPoints = Integer.parseInt(parts[4]);
        return u;
    }

    public String toString() {
        return email + "," + password + "," + role + "," + credit + "," + loyaltyPoints;
    }
}

class Product {
    int id;
    String name;
    double price;
    int quantity;

    Product(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public static Product fromString(String line) {
        String[] parts = line.split(",");
        return new Product(Integer.parseInt(parts[0]), parts[1], Double.parseDouble(parts[2]), Integer.parseInt(parts[3]));
    }

    public String toString() {
        return id + "," + name + "," + price + "," + quantity;
    }
}

class Purchase {
    String email;
    String billDate;
    int billNo;
    double total;

    Purchase(String email, String date, int billNo, double total) {
        this.email = email;
        this.billDate = date;
        this.billNo = billNo;
        this.total = total;
    }

    public String toString() {
        return email + "," + billDate + "," + billNo + "," + total;
    }

    public static Purchase fromString(String line) {
        String[] parts = line.split(",");
        return new Purchase(parts[0], parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]));
    }
}

public class SuperMarketBillingSystem {
    static Scanner scanner = new Scanner(System.in);
    static List<User> users = new ArrayList<>();
    static List<Product> products = new ArrayList<>();
    static List<Purchase> purchases = new ArrayList<>();
    static final String USER_FILE = "users.txt";
    static final String PRODUCT_FILE = "products.txt";
    static final String PURCHASE_FILE = "purchases.txt";
    static Map<Integer, Integer> cart = new HashMap<>();
    static User currentUser = null;

    public static void main(String[] args) {
        loadUsers();
        loadProducts();
        loadPurchases();
        while (true) {
            System.out.println("\n1. Register\n2. Login\n3. Exit\nChoice:");
            int ch = Integer.parseInt(scanner.nextLine());
            if (ch == 1) register();
            else if (ch == 2) {
                if ((currentUser = login()) != null) welcomeMenu();
            }
            else break;
        }
    }

    static void register() {
        System.out.print("Email: "); String email = scanner.nextLine();
        System.out.print("Password: "); String pw = scanner.nextLine();
        System.out.print("Role (admin/customer): "); String role = scanner.nextLine();
        User u = new User(email, pw, role);
        users.add(u); saveUser(u);
        System.out.println("Registered!");
    }

    static User login() {
        System.out.print("Email: "); String email = scanner.nextLine();
        System.out.print("Password: "); String pw = scanner.nextLine();
        for (User u : users) if (u.email.equals(email) && u.password.equals(pw)) return u;
        System.out.println("Invalid login."); return null;
    }

    static void welcomeMenu() {
        if (currentUser.role.equals("admin")) adminMenu();
        else customerMenu();
    }

    static void adminMenu() {
        char ch;
        do {
            System.out.println("\nAdmin Menu: A) Add Product B) Modify C) Delete D) View E) Search F) Reports X) Logout");
            ch = scanner.nextLine().toUpperCase().charAt(0);
            switch (ch) {
                case 'A': addProduct(); break;
                case 'B': modifyProduct(); break;
                case 'C': deleteProduct(); break;
                case 'D': viewProducts(); break;
                case 'E': searchProduct(); break;
                case 'F': adminReports(); break;
            }
        } while (ch != 'X');
    }

    static void customerMenu() {
        char ch;
        do {
            System.out.println("\nCustomer Menu: A) View Products B) Add to Cart C) View Cart D) Edit Cart E) Pay F) History X) Logout");
            ch = scanner.nextLine().toUpperCase().charAt(0);
            switch (ch) {
                case 'A': viewProducts(); break;
                case 'B': addToCart(); break;
                case 'C': viewCart(); break;
                case 'D': editCart(); break;
                case 'E': checkout(); break;
                case 'F': viewHistory(); break;
            }
        } while (ch != 'X');
    }

    static void addProduct() {
        System.out.print("ID: "); int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Name: "); String name = scanner.nextLine();
        System.out.print("Price: "); double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Qty: "); int qty = Integer.parseInt(scanner.nextLine());
        Product p = new Product(id, name, price, qty);
        products.add(p); saveProduct(p);
    }

    static void modifyProduct() {
        System.out.print("ID to modify: "); int id = Integer.parseInt(scanner.nextLine());
        for (Product p : products) {
            if (p.id == id) {
                System.out.print("New Qty: "); p.quantity = Integer.parseInt(scanner.nextLine());
                saveAllProducts(); break;
            }
        }
    }

    static void deleteProduct() {
        System.out.print("ID to delete: "); int id = Integer.parseInt(scanner.nextLine());
        products.removeIf(p -> p.id == id);
        saveAllProducts();
    }

    static void viewProducts() {
        for (Product p : products) {
            System.out.println(p.id + ". " + p.name + " - Rs." + p.price + " (Qty: " + p.quantity + ")");
        }
    }

    static void searchProduct() {
        System.out.print("Search name: ");
        String name = scanner.nextLine();
        for (Product p : products) {
            if (p.name.toLowerCase().contains(name.toLowerCase())) {
                System.out.println(p.id + ". " + p.name + " - Rs." + p.price);
            }
        }
    }

    static void addToCart() {
        System.out.print("Product ID: "); int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Qty: "); int qty = Integer.parseInt(scanner.nextLine());
        cart.put(id, cart.getOrDefault(id, 0) + qty);
    }

    static void viewCart() {
        double total = 0;
        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            for (Product p : products) {
                if (p.id == e.getKey()) {
                    System.out.println(p.name + " x" + e.getValue() + " = Rs." + (p.price * e.getValue()));
                    total += p.price * e.getValue();
                }
            }
        }
        System.out.println("Total: Rs." + total);
    }

    static void editCart() {
        System.out.print("Product ID to update: "); int id = Integer.parseInt(scanner.nextLine());
        System.out.print("New Qty (0 to remove): "); int qty = Integer.parseInt(scanner.nextLine());
        if (qty == 0) cart.remove(id);
        else cart.put(id, qty);
    }

    static void checkout() {
        double total = 0;
        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            for (Product p : products) {
                if (p.id == e.getKey()) {
                    total += p.price * e.getValue();
                    p.quantity -= e.getValue();
                }
            }
        }
        if (total > currentUser.credit) {
            System.out.println("Not enough credit.");
            return;
        }
        currentUser.credit -= total;
        if (total >= 5000) currentUser.credit += 100;
        else currentUser.loyaltyPoints += (int)(total / 100);
        if (currentUser.loyaltyPoints >= 50) {
            currentUser.credit += 100;
            currentUser.loyaltyPoints -= 50;
        }
        int billNo = purchases.size() + 1;
        String date = new Date().toString();
        purchases.add(new Purchase(currentUser.email, date, billNo, total));
        cart.clear(); saveAllProducts(); saveAllUsers(); savePurchase(purchases.get(purchases.size() - 1));
        System.out.println("Checkout successful! Bill No: " + billNo);
    }

    static void viewHistory() {
        for (Purchase p : purchases) {
            if (p.email.equals(currentUser.email))
                System.out.println(p.billNo + " | " + p.billDate + " | Rs." + p.total);
        }
    }

    static void adminReports() {
        System.out.println("\nProducts with low quantity (<5):");
        for (Product p : products) if (p.quantity < 5) System.out.println(p.name);
        System.out.println("\nTop Customers:");
        Map<String, Double> map = new HashMap<>();
        for (Purchase p : purchases) map.put(p.email, map.getOrDefault(p.email, 0.0) + p.total);
        map.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .forEach(e -> System.out.println(e.getKey() + " Rs." + e.getValue()));
    }

    static void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line; while ((line = br.readLine()) != null) users.add(User.fromString(line));
        } catch (Exception ignored) {}
    }

    static void saveUser(User u) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            bw.write(u.toString()); bw.newLine();
        } catch (Exception ignored) {}
    }

    static void saveAllUsers() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User u : users) bw.write(u.toString() + "\n");
        } catch (Exception ignored) {}
    }

    static void loadProducts() {
        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCT_FILE))) {
            String line; while ((line = br.readLine()) != null) products.add(Product.fromString(line));
        } catch (Exception ignored) {}
    }

    static void saveProduct(Product p) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PRODUCT_FILE, true))) {
            bw.write(p.toString()); bw.newLine();
        } catch (Exception ignored) {}
    }

    static void saveAllProducts() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PRODUCT_FILE))) {
            for (Product p : products) bw.write(p.toString() + "\n");
        } catch (Exception ignored) {}
    }

    static void loadPurchases() {
        try (BufferedReader br = new BufferedReader(new FileReader(PURCHASE_FILE))) {
            String line; while ((line = br.readLine()) != null) purchases.add(Purchase.fromString(line));
        } catch (Exception ignored) {}
    }

    static void savePurchase(Purchase p) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PURCHASE_FILE, true))) {
            bw.write(p.toString()); bw.newLine();
        } catch (Exception ignored) {}
    }
}