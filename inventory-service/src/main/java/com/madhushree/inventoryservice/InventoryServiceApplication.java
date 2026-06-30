package com.madhushree.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

}

@RestController
@RequestMapping("/inventory")
class InventoryController{
    @GetMapping("/{productId}")
    public Integer getProductCount(@PathVariable Long productId){

        //pretend this is a database
        Inventory inventory = switch (productId.intValue()) {
            case 1 -> new Inventory(1L, "Wireless Mouse", 799.00, 100);
            case 2 -> new Inventory(2L, "Mechanical Keyboard", 2999.00, 50);
            case 3 -> new Inventory(3L, "HD Monitor", 10999.00, 30);
            default -> throw new RuntimeException("Product not found with id: " + productId);
        };

        return inventory.count();
    }
}

//record InventoryResponse(Long productId, Integer count){}
//record Product(Long id, String name, double price){}
record Inventory(Long id, String name, double price, int count){}


