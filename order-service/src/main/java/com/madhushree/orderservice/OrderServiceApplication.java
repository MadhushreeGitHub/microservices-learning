package com.madhushree.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}

@RestController
@RequestMapping("/orders")
class OrderController{
    private final RestTemplate restTemplate;

    public OrderController(RestTemplate restTemplate){
        this.restTemplate = restTemplate;

    }

    @PostMapping
    public OrderResponse placeOrder(@RequestBody OrderRequest request){
        // ★ THIS LINE is the heart of microservices ★
        // What used to be productService.getProduct(id) is now a network call.

        Product product = restTemplate.getForObject(
                "http://localhost:8081/products/" +  request.productId(),
                Product.class
        );

        Integer count = restTemplate.getForObject(
                "http://localhost:8083/inventory/" + request.productId(),
                Integer.class
        );

        if(count == null || count < request.quantity()){
            throw new RuntimeException("Not enough inventory for product: " + request.productId());
        }
        assert product != null;
        double total = product.price() * request.quantity();

        return new OrderResponse(
                UUID.randomUUID().toString(),
                product.name(),
                request.quantity(),
                total,
                "CONFIRMED"
        );
    }
}

record OrderRequest(Long productId, int quantity){}
record OrderResponse(String orderId, String productName, int quantity, double totalPrice, String status){}
record Product(Long id, String name, double price){}
