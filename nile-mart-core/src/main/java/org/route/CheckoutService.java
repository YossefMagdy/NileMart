package org.route;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductService productService;
    private final ShippingQuoteService shippingQuoteService;
    private final TaxCalculator taxCalculator;
    private final OrderRegistry orderRegistry;
    private final List<NotificationSender> senders;

    public Order placeOrder(ShoppingCart cart, Governorate governorate) {
        if (cart.isEmpty()) {
            throw new StoreException("Cart " + cart.getCartNumber() + " is empty");
        }

        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            Product product = productService.findBySku(line.getKey());
            if (product.getStockQuantity() < line.getValue()) {
                throw new StoreException("Only " + product.getStockQuantity()
                        + " available for " + product.getName());
            }
        }

        BigDecimal subtotal = cart.subtotal();
        BigDecimal vat = taxCalculator.calculate(subtotal);
        BigDecimal shipping = shippingQuoteService.quote(governorate, subtotal);
        BigDecimal total = subtotal.add(vat).add(shipping).setScale(2, RoundingMode.HALF_UP);

        for (Map.Entry<String, Integer> line : cart.getLines().entrySet()) {
            productService.reduceStock(line.getKey(), line.getValue());
        }

        Order order = new Order(
                orderRegistry.nextOrderNumber(),
                cart.getCustomer(),
                governorate,
                Collections.unmodifiableMap(new LinkedHashMap<>(cart.getLines())),
                subtotal, vat, shipping, total);
        orderRegistry.save(order);

        String message = "Order " + order.orderNumber() + " confirmed. Total "
                + OrderRegistry.money(total) + " EGP";
        for (NotificationSender sender : senders) {
            sender.send(cart.getCustomer(), message);
        }

        cart.clear();
        return order;
    }
}
