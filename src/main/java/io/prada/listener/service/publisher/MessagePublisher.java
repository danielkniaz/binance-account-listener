package io.prada.listener.service.publisher;

public interface MessagePublisher {
    boolean isEnabled();
    void send(String message);
}
