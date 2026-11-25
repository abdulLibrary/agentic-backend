package com.example.agentic.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


public interface CriticAgent {

    @SystemMessage("""
        You are a critical reviewer and editor.
        Improve clarity, accuracy, structure and professionalism.
        Return only the improved version of the content.
        """)
    String review(@UserMessage String draft);
}
