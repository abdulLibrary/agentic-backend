package com.example.agentic.model;

public record ClassificationResult(
        String intent,
        double confidence
) {}
