package org.route;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingQuoteService {
        private final ShippingCalculator primary;
        private final ShippingCalculator free;
        private final BigDecimal threshold;

        public ShippingQuoteService(ShippingCalculator primary,
                                    @Qualifier("freeShipping") ShippingCalculator free,
                                    @Value("${store.free-shipping-threshold}") BigDecimal threshold) {
            this.primary = primary;
            this.free = free;
            this.threshold = threshold;
        }

    public BigDecimal quote(Governorate g, BigDecimal subtotal){
            if(subtotal.compareTo(threshold) >= 0 && g.getZone().isFreeShippingEligible()){
                return free.calculate(g);
            }
            return primary.calculate(g);
    }
}
