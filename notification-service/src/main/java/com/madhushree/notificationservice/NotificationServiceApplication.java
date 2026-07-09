package com.madhushree.notificationservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }

}


@Component
class OrderNotificationListener{
    @RabbitListener(queues = "order.placed.queue")
    public void onOrderPlaced(OrderPlacedEvent event){
        // Simulating email/SMS/push
        System.out.println("┌─────────────────────────────────────────────");
        System.out.println("│ 📧 EMAIL SENT");
        System.out.println("│ Order ID:      " + event.orderId());
        System.out.println("│ Product:       " + event.productName());
        System.out.println("│ Quantity:      " + event.quantity());
        System.out.println("│ Total Amount:  ₹" + event.totalAmount());
        System.out.println("└─────────────────────────────────────────────");
    }
}

// ★ NOTE: A DUPLICATE OF THE PUBLISHER'S EVENT CLASS ★
// This is the "contracts not classes" principle in action.
// Same shape (fields + names) so JSON/serialization matches — but a separate class.
record  OrderPlacedEvent(String orderId, String productName, int quantity, double totalAmount) {}
