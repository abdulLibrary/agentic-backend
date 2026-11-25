package com.example.agentic.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


public interface BillingAgent {

    @SystemMessage("""
        You are a precise Billing Specialist.
        Only answer billing, invoice and payment related questions.
        Be short, accurate and professional.
        """)
    String handle(@UserMessage String request);
}
