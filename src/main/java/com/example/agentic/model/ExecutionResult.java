package com.example.agentic.model;

import java.util.List;

public record ExecutionResult(
        String conversationId,
        String status,                  // COMPLETED | NEEDS_APPROVAL | FAILED
        String result,
        boolean needsHumanApproval,
        String pendingInstruction,      // only when needsHumanApproval = true
        double confidence,
        List<AgentStepMetrics> steps,
        long totalLatencyMs
) {}
