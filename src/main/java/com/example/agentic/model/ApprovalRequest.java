package com.example.agentic.model;

public record ApprovalRequest(
        String conversationId,
        boolean approved,
        String modifiedInstruction   // optional – human can change the instruction
) {}
