package com.example.agentic.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class ResearchTools {

    @Tool("Search internal knowledge base for relevant information")
    public String searchKnowledgeBase(@P("The search query") String query) {
        // In real systems replace this with vector store / database call
        return "Knowledge base result for query: " + query +
               " | Sample data: Spring Boot 3.4 includes improved observability, native image support enhancements, and updated dependencies.";
    }

    @Tool("Get current date and time")
    public String getCurrentDateTime() {
        return java.time.LocalDateTime.now().toString();
    }
}
