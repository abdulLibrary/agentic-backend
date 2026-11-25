package com.example.agentic.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the correct EmbeddingStore based on configuration.
 * Supported: memory | qdrant | pgvector
 */
@Configuration
@Slf4j
public class VectorStoreConfig {

    @Value("${vector-store.type:memory}")
    private String storeType;

    // Qdrant
    @Value("${vector-store.qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${vector-store.qdrant.port:6333}")
    private int qdrantPort;

    @Value("${vector-store.qdrant.collection:knowledge-base}")
    private String qdrantCollection;

    // PGVector
    @Value("${vector-store.pgvector.host:localhost}")
    private String pgHost;

    @Value("${vector-store.pgvector.port:5432}")
    private int pgPort;

    @Value("${vector-store.pgvector.database:agentic}")
    private String pgDatabase;

    @Value("${vector-store.pgvector.user:postgres}")
    private String pgUser;

    @Value("${vector-store.pgvector.password:postgres}")
    private String pgPassword;

    @Value("${vector-store.pgvector.table:embeddings}")
    private String pgTable;

    @Value("${vector-store.pgvector.dimension:384}")
    private int pgDimension;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
        return switch (storeType.toLowerCase()) {
            case "qdrant" -> {
                log.info("Using Qdrant EmbeddingStore → {}:{} / {}", qdrantHost, qdrantPort, qdrantCollection);
                yield QdrantEmbeddingStore.builder()
                        .host(qdrantHost)
                        .port(qdrantPort)
                        .collectionName(qdrantCollection)
                        .build();
            }
            case "pgvector" -> {
                log.info("Using PGVector EmbeddingStore → {}:{}/{}", pgHost, pgPort, pgDatabase);
                yield PgVectorEmbeddingStore.builder()
                        .host(pgHost)
                        .port(pgPort)
                        .database(pgDatabase)
                        .user(pgUser)
                        .password(pgPassword)
                        .table(pgTable)
                        .dimension(pgDimension)
                        .build();
            }
            default -> {
                log.info("Using InMemory EmbeddingStore (default)");
                yield new InMemoryEmbeddingStore<>();
            }
        };
    }
}
