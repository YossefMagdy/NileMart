package org.route;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class ShoppingCart {

    private final ProductService productService;
    private final int maxQuantity;

    private final Map<String, Integer> lines = new LinkedHashMap<>();

    @Getter private int cartNumber;
    @Getter private Customer customer;

    public ShoppingCart(ProductService productService,
                        @Value("${store.max-quantity-per-product}") int maxQuantity) {
        this.productService = productService;
        this.maxQuantity = maxQuantity;
    }

    public void assign(int cartNumber, Customer customer) {
        this.cartNumber = cartNumber;
        this.customer = customer;
    }

    public void addItem(String sku, int qty) {
        if (qty <= 0) {
            throw new StoreException("Quantity must be positive");
        }

        Product product = productService.findBySku(sku);
        int stock = product.getStockQuantity();

        if (stock == 0) {
            throw new StoreException(product.getName() + " is out of stock");
        }

        int newQty = lines.getOrDefault(sku, 0) + qty;

        if (newQty > maxQuantity) {
            throw new StoreException("Max " + maxQuantity + " per product (cart would have " + newQty + ")");
        }
        if (newQty > stock) {
            throw new StoreException("Only " + stock + " available for " + product.getName());
        }

        lines.put(sku, newQty);
    }

    public BigDecimal subtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> line : lines.entrySet()) {
            total = total.add(productService.getPrice(line.getKey(), line.getValue()));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }

    public Map<String, Integer> getLines() {
        return Collections.unmodifiableMap(lines);
    }
}
