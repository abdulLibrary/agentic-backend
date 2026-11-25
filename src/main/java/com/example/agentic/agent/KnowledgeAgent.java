package com.example.agentic.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;


/**
 * RAG-powered Knowledge Agent.
 * Answers questions using retrieved context from the vector store.
 */

public interface KnowledgeAgent {

    @SystemMessage("""
        You are a Knowledge Base assistant powered by Retrieval-Augmented Generation (RAG).
        Answer the user question using ONLY the provided context when possible.
        If the context does not contain enough information, say so clearly.
        Be concise, accurate and cite relevant parts of the context.
        """)
    String answer(@UserMessage String question);
}
