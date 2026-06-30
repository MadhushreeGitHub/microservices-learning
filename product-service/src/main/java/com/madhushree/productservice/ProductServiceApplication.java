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
            1L, new Product(1L, "Wireless Mouse", 799.00),
            2L, new Product(2L, "Mechanical Keyboard", 2999.00),
            3L, new Product(3L, "HD Monitor", 10999.00)
    );

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) throws InterruptedException {
        Product product = PRODUCTS.get(id);
        //Thread.sleep(10000);
        if(product == null){
            throw new RuntimeException("Product not found with id: " + id);
        }
        return product;
    }

}

record Product(Long id, String name, double price){}