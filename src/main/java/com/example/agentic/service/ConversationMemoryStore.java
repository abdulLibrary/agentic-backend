package com.example.agentic.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory conversation store.
 * Key = conversationId, Value = accumulated state / history.
 * In production replace with Redis / DB.
 */
@Component
public class ConversationMemoryStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final Map<String, PendingApproval> pendingApprovals = new ConcurrentHashMap<>();

    public void save(String conversationId, String state) {
        store.put(conversationId, state);
    }

    public String get(String conversationId) {
        return store.getOrDefault(conversationId, "");
    }

    public void clear(String conversationId) {
        store.remove(conversationId);
        pendingApprovals.remove(conversationId);
    }

    public void savePendingApproval(String conversationId, PendingApproval approval) {
        pendingApprovals.put(conversationId, approval);
    }

    public PendingApproval getPendingApproval(String conversationId) {
        return pendingApprovals.get(conversationId);
    }

    public void removePendingApproval(String conversationId) {
        pendingApprovals.remove(conversationId);
    }

    public record PendingApproval(
            String originalGoal,
            String currentState,
            String suggestedInstruction,
            double confidence,
            String agentType
    ) {}
}
