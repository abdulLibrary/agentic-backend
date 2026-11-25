package com.example.agentic.controller;

import com.example.agentic.model.ApprovalRequest;
import com.example.agentic.model.ExecutionResult;
import com.example.agentic.service.DynamicSupervisor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final DynamicSupervisor supervisor;

    /**
     * Main execution endpoint.
     * Supports conversation continuity via conversationId.
     */
    @PostMapping("/execute")
    public ExecutionResult execute(@RequestBody Map<String, String> body) {
        String goal = body.getOrDefault("goal", body.get("message"));
        String conversationId = body.get("conversationId");

        if (goal == null || goal.isBlank()) {
            return new ExecutionResult(
                    null, "FAILED", "Please provide a 'goal' or 'message' field",
                    false, null, 0, java.util.List.of(), 0
            );
        }

        return supervisor.execute(goal, conversationId);
    }

    /**
     * Human-in-the-loop approval endpoint.
     */
    @PostMapping("/approve")
    public ExecutionResult approve(@RequestBody ApprovalRequest request) {
        return supervisor.continueAfterApproval(
                request.conversationId(),
                request.approved(),
                request.modifiedInstruction()
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "agentic-backend",
                "version", "1.1.0"
        );
    }
}
