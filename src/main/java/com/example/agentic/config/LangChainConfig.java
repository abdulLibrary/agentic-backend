package com.example.agentic.config;

import com.example.agentic.agent.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChainConfig {

    @Value("${ai.provider:openai}")
    private String provider;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2}")
    private String ollamaModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if ("ollama".equalsIgnoreCase(provider)) {
            return OllamaChatModel.builder()
                    .baseUrl(ollamaBaseUrl)
                    .modelName(ollamaModel)
                    .temperature(0.2)
                    .timeout(Duration.ofMinutes(5))
                    .build();
        }

        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(openAiModel)
                .temperature(0.2)
                .build();
    }

    /**
     * Local embedding model – works offline and with Ollama.
     * For higher quality you can later switch to OpenAiEmbeddingModel.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public ResearcherAgent researcherAgent(ChatLanguageModel model) {
        return AiServices.create(ResearcherAgent.class, model);
    }

    @Bean
    public WriterAgent writerAgent(ChatLanguageModel model) {
        return AiServices.create(WriterAgent.class, model);
    }

    @Bean
    public CriticAgent criticAgent(ChatLanguageModel model) {
        return AiServices.create(CriticAgent.class, model);
    }

    @Bean
    public BillingAgent billingAgent(ChatLanguageModel model) {
        return AiServices.create(BillingAgent.class, model);
    }

    @Bean
    public KnowledgeAgent knowledgeAgent(ChatLanguageModel model) {
        return AiServices.create(KnowledgeAgent.class, model);
    }

    @Bean
    public SupervisorAgent supervisorAgent(ChatLanguageModel model) {
        return AiServices.create(SupervisorAgent.class, model);
    }
}
