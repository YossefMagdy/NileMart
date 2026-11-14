package org.route;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class OrderRegistry {

    private final List<Order> orders = new ArrayList<>();
    private int counter = 0;

    public String nextOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "NM-" + date + "-" + String.format("%05d", ++counter);
    }

    public void save(Order order) {
        orders.add(order);
    }

    public List<Order> findAll() {
        return List.copyOf(orders);
    }

    @PreDestroy
    public void endOfDay() {
        BigDecimal revenue = BigDecimal.ZERO;
        for (Order order : orders) {
            revenue = revenue.add(order.total());
        }
        System.out.println("End of day: " + orders.size() + " orders, revenue " + money(revenue) + " EGP");
    }

    public static String money(BigDecimal amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }
}
