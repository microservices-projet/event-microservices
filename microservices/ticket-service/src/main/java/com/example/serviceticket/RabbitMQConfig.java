//package com.example.serviceticket;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
////import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.support.converter.MessageConverter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//@Configuration
//public class RabbitMQConfig {
//    // Nom de la queue
//    public static final String EVENT_QUEUE = "EVENT_QUEUE";
//
//    @Bean
//    public Queue eventQueue() {
//        return new Queue(EVENT_QUEUE, true);
//    }
////
////    @Bean
////    public Jackson2JsonMessageConverter messageConverter() {
////        return new Jackson2JsonMessageConverter();
////    }
//
//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setMessageConverter(messageConverter());
//        return factory;
//    }
//}
