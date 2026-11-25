package com.example.agentic.model;

public record NextAction(
        AgentType nextAgent,
        String instruction,
        double confidence
) {}
