package com.madhushree.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(ProductServiceApplication.class, args);
    }

}

@RestController
@RequestMapping("/products")
class ProductController{
    //pretend this is a database
    private static final Map<Long,Product> PRODUCTS = Map.of(
            1L, new Product(1L, "Wireless Mouse", 799.00, 400, "SUP-101", "check notes"),
            2L, new Product(2L, "Mechanical Keyboard", 2999.00, 1500, "SUP-102", "low stock"),
            3L, new Product(3L, "HD Monitor", 10999.00, 5000, "SUP-103", "out of stock")
    );

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) throws InterruptedException {
        Product product = PRODUCTS.get(id);
        //Thread.sleep(10000);
        if(product == null){
            throw new RuntimeException("Product not found with id: " + id);
        }
        return new ProductResponse(product.id(), product.name(), product.price());
    }

}

//Internal model
record Product(
        Long id,
        String name,
        double price,
        double costPrice, //Internal: how much we pay to supplier
        String supplierId, //Internal: which supplier
        String internalNotes //Internal: warehouse comments
){}

// External contract — what OTHER services see

record ProductResponse(
        Long id,
        String name,
        double price
){}