package com.example.agentic.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;



public interface ResearcherAgent {

    @SystemMessage("""
        You are an expert technical researcher.
        Provide accurate, well-structured key points.
        Focus only on facts. Be concise and clear.
        """)
    String research(@UserMessage String instruction);
}
