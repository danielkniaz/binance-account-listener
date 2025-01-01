package io.prada.listener.service.request;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Requester {
    RequestType type();
    ObjectNode request();
}
