package com.madhushree.orderservice;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import java.time.Duration;

import java.util.Map;
import org.springframework.amqp.core.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public TopicExchange orderExchange(){
        return new TopicExchange("order.exchange");
    }
    @Bean
    public Queue orderPlacedQueue(){
        return new Queue("order.placed.queue", true);
    }

    @Bean
    public Binding orderPlaceBinding(){
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(orderExchange())
                .with("order.placed");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder
                .connectTimeout(Duration.ofSeconds(2)) // time to establish TCP connection
                .readTimeout(Duration.ofSeconds(3))  // time to receive response
                .build();
    }

}

@RestController
@RequestMapping("/orders")
class OrderController{

    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final Map<String, OrderResponse> idempotencyCache = new ConcurrentHashMap<>();

    public OrderController(RestTemplate restTemplate, RabbitTemplate rabbitTemplate){
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody OrderRequest request){
        // ★ THIS LINE is the heart of microservices ★
        // What used to be productService.getProduct(id) is now a network call.

        // If key was seen before, return the cached response
        if(idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)){
            return ResponseEntity.ok(idempotencyCache.get(idempotencyKey));
        }

        try{
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

            OrderResponse response = new OrderResponse(
                    UUID.randomUUID().toString(),
                    product.name(),
                    request.quantity(),
                    total,
                    "CONFIRMED"
            );
            //*PUBLISH THE EVENT*
            OrderPlacedEvent event = new OrderPlacedEvent(
                    response.orderId(),
                    product.name(),
                    request.quantity(),
                    total
            );

            rabbitTemplate.convertAndSend(
                    "order.exchange",
                    "order.placed",
                    event
            );

            if (idempotencyKey != null) {
                idempotencyCache.put(idempotencyKey, response);
            }

            return ResponseEntity.ok(response);
        }
        catch (ResourceAccessException e) {
            // Timeout or connection refused
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Product service unavailable", "orderStatus", "REJECTED"));
        }
    }
}

record OrderRequest(Long productId, int quantity){}
record OrderResponse(String orderId, String productName, int quantity, double totalPrice, String status) implements java.io.Serializable{}
record Product(Long id, String name, double price){}
//Event class -must be serializable OR use a JSON converter
record OrderPlacedEvent(String orderId, String productName, int quantity, double totalAmount){}
