package com.example.agentic.model;

import java.time.Instant;

public record AgentStepMetrics(
        int step,
        AgentType agent,
        String instruction,
        double confidence,
        long latencyMs,
        int inputTokens,      // approximate
        int outputTokens,     // approximate
        Instant timestamp
) {}
