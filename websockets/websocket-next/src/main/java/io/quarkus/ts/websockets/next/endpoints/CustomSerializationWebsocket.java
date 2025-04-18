package io.quarkus.ts.websockets.next.endpoints;

import java.lang.reflect.Type;

import jakarta.inject.Singleton;

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.TextMessageCodec;
import io.quarkus.websockets.next.WebSocket;

@WebSocket(path = "/serialization/custom")
public class CustomSerializationWebsocket {

    @OnTextMessage(codec = ChatMessageCodec.class)
    public ChatMessage onMessage(ChatMessage inputMessage) {
        return new ChatMessage(
                inputMessage.author,
                "received: " + inputMessage.message);
    }

    public record ChatMessage(
            String author,
            String message) {
    }

    @Singleton
    public static class ChatMessageCodec implements TextMessageCodec<ChatMessage> {
        @Override
        public boolean supports(Type type) {
            return type.equals(ChatMessage.class);
        }

        @Override
        public String encode(ChatMessage value) {
            return value.author + ";" + value.message;
        }

        @Override
        public ChatMessage decode(Type type, String value) {
            String[] input = value.split(";");
            if (input.length != 2) {
                throw new RuntimeException("Invalid input string: " + value);
            }
            return new ChatMessage(input[0], input[1]);
        }
    }
}
