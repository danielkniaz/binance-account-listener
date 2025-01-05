package io.prada.listener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {
    private final RabbitTemplate template;
    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String topic;

    public void send(String message) {
        try {
            template.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("error sending message: {}", e.getMessage(), e);
        }
    }
}
