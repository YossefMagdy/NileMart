package org.route;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private final Scanner in = new Scanner(System.in);
    private final Map<Integer, ShoppingCart> carts = new LinkedHashMap<>();
    private int nextCartNumber = 1;

    void main() {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            ProductService productService = context.getBean(ProductService.class);
            CheckoutService checkoutService = context.getBean(CheckoutService.class);

            while (true) {
                printMenu();
                String choice = in.nextLine().trim();
                if (choice.equals("0")) {
                    break;
                }
                try {
                    switch (choice) {
                        case "1" -> showProducts(productService);
                        case "2" -> createCart(context);
                        case "3" -> addProduct();
                        case "4" -> showCart();
                        case "5" -> checkout(checkoutService);
                        default -> System.out.println("Unknown option: " + choice);
                    }
                } catch (StoreException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            System.out.println("Bye!");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("========== NILEMART ==========");
        System.out.println("1. Show products");
        System.out.println("2. Create a cart");
        System.out.println("3. Add a product to a cart");
        System.out.println("4. Show a cart");
        System.out.println("5. Check out a cart");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    private void showProducts(ProductService productService) {
        System.out.printf("%-12s %-28s %-15s %12s %6s%n", "SKU", "Name", "Category", "Price (EGP)", "Stock");
        for (Product p : productService.findAll()) {
            System.out.printf("%-12s %-28s %-15s %12s %6d%n",
                    p.getSku(), p.getName(), p.getCategory(), money(p.getPrice()), p.getStockQuantity());
        }
    }

    private void createCart(AnnotationConfigApplicationContext context) {
        String name = readText("Full name: ");
        String phone = readText("Phone: ");
        String email = readText("Email (leave empty if none): ");
        Customer customer = new Customer(name, phone, email.isEmpty() ? null : email);

        ShoppingCart cart = context.getBean(ShoppingCart.class);
        cart.assign(nextCartNumber, customer);
        carts.put(nextCartNumber, cart);
        System.out.println("Cart " + nextCartNumber + " created for " + name);
        nextCartNumber++;
    }

    private void addProduct() {
        ShoppingCart cart = findCart(readInt("Cart number: "));
        String sku = readText("SKU: ").toUpperCase();
        int qty = readInt("Quantity: ");
        cart.addItem(sku, qty);
        System.out.println("Added " + qty + " x " + sku + " to cart " + cart.getCartNumber());
    }

    private void showCart() {
        ShoppingCart cart = findCart(readInt("Cart number: "));
        System.out.println("Cart " + cart.getCartNumber() + " - " + cart.getCustomer().fullName());
        if (cart.isEmpty()) {
            System.out.println("  (empty)");
            return;
        }
        cart.getLines().forEach((sku, qty) -> System.out.println("  " + qty + " x " + sku));
        System.out.println("  Subtotal: " + money(cart.subtotal()) + " EGP");
    }

    private void checkout(CheckoutService checkoutService) {
        ShoppingCart cart = findCart(readInt("Cart number: "));
        Governorate governorate = readGovernorate();
        Order order = checkoutService.placeOrder(cart, governorate);

        System.out.println("Order " + order.orderNumber() + " -> " + governorate.getDisplayName());
        System.out.println("  Subtotal: " + money(order.subtotal()) + " EGP");
        System.out.println("  VAT:      " + money(order.vat()) + " EGP");
        System.out.println("  Shipping: " + money(order.shippingFee()) + " EGP");
        System.out.println("  Total:    " + money(order.total()) + " EGP");
    }

    private ShoppingCart findCart(int number) {
        ShoppingCart cart = carts.get(number);
        if (cart == null) {
            throw new StoreException("Cart " + number + " not found");
        }
        return cart;
    }

    private Governorate readGovernorate() {
        Governorate[] all = Governorate.values();
        for (int i = 0; i < all.length; i++) {
            System.out.printf("%2d. %-16s", i + 1, all[i].getDisplayName());
            if ((i + 1) % 3 == 0) System.out.println();
        }
        System.out.println();
        int n = readInt("Governorate number: ");
        if (n < 1 || n > all.length) {
            throw new StoreException("Governorate must be between 1 and " + all.length);
        }
        return all[n - 1];
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private int readInt(String prompt) {
        String text = readText(prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new StoreException("'" + text + "' is not a number");
        }
    }

    private static String money(BigDecimal amount) {
        return OrderRegistry.money(amount);
    }
}
