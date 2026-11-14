package org.route;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final PricingService pricingService;


    public  List<Product> findAll(){
        return repository.findAll();
    }

    public  Product findBySku(String sku){
        return repository.findBySku(sku).orElseThrow(()->new StoreException("Product " + sku + " not found"));
    }

    public  void reduceStock(String sku,int qty){
        Product product = findBySku(sku);

        if(product.getStockQuantity() < qty){
            throw new StoreException("only " + product.getStockQuantity() + " available for " + product.getName());
        }

            product.setStockQuantity(product.getStockQuantity() - qty);
            repository.save(product);
    }

    public  BigDecimal getPrice(String sku, int qty){
        Product product = findBySku(sku);
        return this.pricingService.getPrice(product,qty);

    }
}
