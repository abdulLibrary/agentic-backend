package com.example.agentic.controller;

import com.example.agentic.model.ExecutionResult;
import com.example.agentic.service.DynamicSupervisor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final DynamicSupervisor supervisor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = objectMapper.readTree(message.getPayload());
        String type = node.has("type") ? node.get("type").asText() : "chat";

        if ("chat".equals(type)) {
            String goal = node.get("goal").asText();
            String conversationId = node.has("conversationId") ? node.get("conversationId").asText() : null;

            session.sendMessage(new TextMessage("{\"type\":\"status\",\"content\":\"Thinking...\"}"));

            ExecutionResult result = supervisor.execute(goal, conversationId);

            // Send full result as JSON
            String json = objectMapper.writeValueAsString(result);
            session.sendMessage(new TextMessage("{\"type\":\"result\",\"data\":" + json + "}"));

            // Also stream tokens if completed
            if ("COMPLETED".equals(result.status())) {
                for (String word : result.result().split(" ")) {
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"token\",\"content\":\"" + escape(word) + " \"}"));
                    Thread.sleep(20);
                }
            }

            session.sendMessage(new TextMessage("{\"type\":\"done\"}"));
        }
        else if ("approve".equals(type)) {
            String conversationId = node.get("conversationId").asText();
            boolean approved = node.get("approved").asBoolean();
            String modified = node.has("modifiedInstruction") ? node.get("modifiedInstruction").asText() : null;

            ExecutionResult result = supervisor.continueAfterApproval(conversationId, approved, modified);
            String json = objectMapper.writeValueAsString(result);
            session.sendMessage(new TextMessage("{\"type\":\"result\",\"data\":" + json + "}"));
            session.sendMessage(new TextMessage("{\"type\":\"done\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket closed: {}", session.getId());
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
