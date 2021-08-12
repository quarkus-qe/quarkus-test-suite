package io.quarkus.ts.http.advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class GrpcWebSocketListener extends WebSocketListener {

    static Map<Integer, List<String>> serviceOutputMessagesMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        try {
            ObjectNode node = objectMapper.readValue(text, ObjectNode.class);
            int id = node.get("id").asInt();
            List<String> list = serviceOutputMessagesMap.computeIfAbsent(id, k -> new ArrayList<>());
            synchronized (list) {
                list.add(text);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
