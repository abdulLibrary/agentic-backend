package com.example.agentic.agent;

import com.example.agentic.model.NextAction;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;



public interface SupervisorAgent {

    @SystemMessage("""
        You are a highly intelligent supervisor managing a team of specialized agents.
        Available agents: RESEARCHER, WRITER, CRITIC, BILLING, KNOWLEDGE, FINISH.
        
        - RESEARCHER: gather information from general knowledge
        - WRITER: write clear summaries or content
        - CRITIC: review and improve content
        - BILLING: handle invoice / payment questions
        - KNOWLEDGE: answer questions using the internal knowledge base (RAG)
        - FINISH: when the goal is fully achieved
        
        Based on the current state, decide the next best agent and give it a clear instruction.
        Also provide a confidence score between 0.0 and 1.0.
        
        Respond STRICTLY in pure JSON format only (no markdown, no extra text):
        {
          "nextAgent": "KNOWLEDGE",
          "instruction": "What is the leave policy?",
          "confidence": 0.91
        }
        
        When the goal is fully achieved, return nextAgent as FINISH.
        """)
    NextAction decide(@UserMessage String currentState);
}
