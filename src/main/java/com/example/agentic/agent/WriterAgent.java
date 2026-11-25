package com.example.agentic.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;



public interface WriterAgent {

    @SystemMessage("""
        You are a senior technical writer.
        Write clear, professional and concise content.
        Use markdown when it improves readability.
        """)
    String write(@UserMessage String instruction);
}
