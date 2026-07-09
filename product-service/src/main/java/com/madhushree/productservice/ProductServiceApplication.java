package com.madhushree.productservice;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(ProductServiceApplication.class, args);
    }

}

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog operations")
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

    @GetMapping("/{id}/internal")
    public Product getInternalProduct(@PathVariable Long id,
        @RequestHeader(value = "X-Internal-Client", required = false) boolean internalClientHeader) {
        Product product = PRODUCTS.get(id);
        if(!internalClientHeader){
            throw new RuntimeException("This not a Internal Client");
        }
        return product;
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