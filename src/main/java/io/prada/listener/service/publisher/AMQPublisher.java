package io.prada.listener.service.publisher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AMQPublisher implements MessagePublisher  {
    private final RabbitTemplate template;

    @Value("${spring.rabbitmq.template.default-receive-queue}")
    private String topic;
    @Getter
    @Value("${settings.publisher.amq:false}")
    private boolean enabled;

    public void send(String message) {
        try {
            template.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("error sending message: {}", e.getMessage(), e);
        }
    }
}
