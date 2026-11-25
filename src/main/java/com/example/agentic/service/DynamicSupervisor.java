package com.example.agentic.service;

import com.example.agentic.agent.*;
import com.example.agentic.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicSupervisor {

    private final SupervisorAgent supervisor;
    private final ResearcherAgent researcher;
    private final WriterAgent writer;
    private final CriticAgent critic;
    private final BillingAgent billingAgent;
    private final KnowledgeAgent knowledgeAgent;
    private final RagService ragService;
    private final ConversationMemoryStore memoryStore;

    @Value("${agent.confidence-threshold:0.72}")
    private double confidenceThreshold;

    @Value("${agent.max-steps:7}")
    private int maxSteps;

    @Value("${agent.human-in-the-loop:true}")
    private boolean humanInTheLoop;

    public ExecutionResult execute(String userGoal, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String previousState = memoryStore.get(conversationId);
        String state = previousState.isBlank()
                ? "User Goal: " + userGoal + "\n\nProgress so far: None"
                : previousState + "\n\nNew User Message: " + userGoal;

        List<AgentStepMetrics> steps = new ArrayList<>();
        long totalStart = System.currentTimeMillis();
        String finalAnswer = null;

        for (int step = 1; step <= maxSteps; step++) {
            log.info("===== [{}] Supervisor Step {} =====", conversationId, step);

            long stepStart = System.currentTimeMillis();
            NextAction action = supervisor.decide(state);
            long stepLatency = System.currentTimeMillis() - stepStart;

            int inputTokens = estimateTokens(state);
            int outputTokens = estimateTokens(action.instruction() != null ? action.instruction() : "");

            steps.add(new AgentStepMetrics(
                    step,
                    action.nextAgent(),
                    action.instruction(),
                    action.confidence(),
                    stepLatency,
                    inputTokens,
                    outputTokens,
                    Instant.now()
            ));

            log.info("Decision → {} | Confidence: {} | Latency: {}ms",
                    action.nextAgent(), action.confidence(), stepLatency);

            // Low confidence → Human-in-the-loop
            if (action.confidence() < confidenceThreshold) {
                if (humanInTheLoop) {
                    memoryStore.save(conversationId, state);
                    memoryStore.savePendingApproval(conversationId,
                            new ConversationMemoryStore.PendingApproval(
                                    userGoal,
                                    state,
                                    action.instruction(),
                                    action.confidence(),
                                    action.nextAgent().name()
                            ));

                    return new ExecutionResult(
                            conversationId,
                            "NEEDS_APPROVAL",
                            "Low confidence (" + String.format("%.0f%%", action.confidence() * 100) +
                                    "). Waiting for human approval.",
                            true,
                            action.instruction(),
                            action.confidence(),
                            steps,
                            System.currentTimeMillis() - totalStart
                    );
                } else {
                    return new ExecutionResult(
                            conversationId,
                            "FAILED",
                            lowConfidenceMessage(userGoal, action),
                            false,
                            null,
                            action.confidence(),
                            steps,
                            System.currentTimeMillis() - totalStart
                    );
                }
            }

            if (action.nextAgent() == AgentType.FINISH) {
                finalAnswer = extractFinal(state);
                break;
            }

            String output = executeAgent(action);
            state += "\n\n--- " + action.nextAgent() + " ---\n" + output;
            memoryStore.save(conversationId, state);
        }

        long totalLatency = System.currentTimeMillis() - totalStart;

        return new ExecutionResult(
                conversationId,
                "COMPLETED",
                finalAnswer != null ? finalAnswer : state,
                false,
                null,
                steps.isEmpty() ? 1.0 : steps.get(steps.size() - 1).confidence(),
                steps,
                totalLatency
        );
    }

    public ExecutionResult continueAfterApproval(String conversationId, boolean approved, String modifiedInstruction) {
        var pending = memoryStore.getPendingApproval(conversationId);
        if (pending == null) {
            return new ExecutionResult(conversationId, "FAILED",
                    "No pending approval found for this conversation", false, null, 0, List.of(), 0);
        }

        if (!approved) {
            memoryStore.removePendingApproval(conversationId);
            return new ExecutionResult(conversationId, "CANCELLED",
                    "Execution cancelled by human", false, null, pending.confidence(), List.of(), 0);
        }

        String instruction = (modifiedInstruction != null && !modifiedInstruction.isBlank())
                ? modifiedInstruction
                : pending.suggestedInstruction();

        String state = pending.currentState();
        AgentType agentType = AgentType.valueOf(pending.agentType());

        String output = executeAgent(new NextAction(agentType, instruction, 1.0));
        state += "\n\n--- " + agentType + " (after human approval) ---\n" + output;
        memoryStore.save(conversationId, state);
        memoryStore.removePendingApproval(conversationId);

        return execute("Continue from approved step", conversationId);
    }

    private String executeAgent(NextAction action) {
        return switch (action.nextAgent()) {
            case RESEARCHER -> researcher.research(action.instruction());
            case WRITER     -> writer.write(action.instruction());
            case CRITIC     -> critic.review(action.instruction());
            case BILLING    -> billingAgent.handle(action.instruction());
            case KNOWLEDGE  -> {
                // RAG: retrieve context then ask the KnowledgeAgent
                String context = ragService.retrieveContext(action.instruction(), 4);
                String prompt = "Context from knowledge base:\n\n" + context +
                        "\n\nQuestion: " + action.instruction();
                yield knowledgeAgent.answer(prompt);
            }
            default -> "No action taken";
        };
    }

    private String lowConfidenceMessage(String goal, NextAction action) {
        return """
               I am not confident enough to proceed automatically (confidence: %.0f%%).
               
               Original goal: %s
               Suggested next step was: %s
               
               Please clarify or choose:
               1. Continue with research
               2. Billing related
               3. Knowledge base question
               4. Just write a summary
               """.formatted(action.confidence() * 100, goal, action.instruction());
    }

    private String extractFinal(String state) {
        int idx = state.lastIndexOf("---");
        return idx > 0 ? state.substring(idx + 3).trim() : state;
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) return 0;
        return Math.max(1, text.length() / 4);
    }
}
