package com.example.agentic.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG service that works with any EmbeddingStore
 * (InMemory, Qdrant, PGVector, etc.)
 */
@Service
@Slf4j
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingStoreIngestor ingestor;

    public RagService(EmbeddingModel embeddingModel,
                      EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;

        DocumentSplitter splitter = DocumentSplitters.recursive(400, 40);

        this.ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    @PostConstruct
    public void init() {
        // Seed sample knowledge only when using in-memory store
        if (embeddingStore.getClass().getSimpleName().contains("InMemory")) {
            seedSampleKnowledge();
        } else {
            log.info("Skipping sample seed – using persistent vector store: {}",
                    embeddingStore.getClass().getSimpleName());
        }
    }

    public void ingestText(String text, String sourceName) {
        Document doc = Document.from(text);
        doc.metadata().put("source", sourceName != null ? sourceName : "manual");
        ingestor.ingest(doc);
        log.info("Ingested document from source: {}", sourceName);
    }

    public void ingestDocuments(List<String> texts) {
        for (int i = 0; i < texts.size(); i++) {
            ingestText(texts.get(i), "doc-" + (i + 1));
        }
    }

    public String retrieveContext(String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.findRelevant(queryEmbedding, maxResults, 0.55);

        if (matches.isEmpty()) {
            return "No relevant information found in the knowledge base.";
        }

        return matches.stream()
                .map(match -> {
                    String source = match.embedded().metadata().getString("source");
                    return "[Source: " + (source != null ? source : "unknown") +
                            " | Score: " + String.format("%.2f", match.score()) + "]\n" +
                            match.embedded().text();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private void seedSampleKnowledge() {
        String springBootKnowledge = """
                Spring Boot 3.4 Key Features:
                
                1. Improved Observability
                   - Enhanced Micrometer support
                   - Better OpenTelemetry integration
                   - New observations for HTTP client and server
                
                2. Native Image Improvements
                   - Faster native image builds
                   - Reduced memory footprint
                   - Better GraalVM compatibility
                
                3. Dependency Upgrades
                   - Spring Framework 6.2
                   - Tomcat 10.1.x updates
                   - Updated Jackson and Hibernate versions
                
                4. Virtual Threads Support
                   - Better support for Project Loom virtual threads
                   - Recommended for high-concurrency applications
                
                5. Security Enhancements
                   - Improved OAuth2 client support
                   - New security matchers and filters
                """;

        String companyPolicy = """
                Internal Company Knowledge Base:
                
                Leave Policy:
                - Annual leave: 24 days
                - Sick leave: 12 days
                - Work from home: up to 8 days per month
                
                Expense Policy:
                - Meal allowance during travel: $50/day
                - Laptop reimbursement: up to $1500 every 3 years
                - Internet allowance: $30/month
                
                Support Escalation:
                - P1 issues: respond within 15 minutes
                - P2 issues: respond within 2 hours
                - P3 issues: respond within 1 business day
                """;

        ingestText(springBootKnowledge, "spring-boot-3.4-docs");
        ingestText(companyPolicy, "company-handbook");
        log.info("Seeded sample knowledge base documents (InMemory mode)");
    }
}
