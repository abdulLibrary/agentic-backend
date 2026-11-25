package com.example.agentic.controller;

import com.example.agentic.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    /**
     * Ingest a single document into the knowledge base.
     */
    @PostMapping("/ingest")
    public Map<String, Object> ingest(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String source = body.getOrDefault("source", "manual-upload");

        if (text == null || text.isBlank()) {
            return Map.of("status", "error", "message", "text is required");
        }

        ragService.ingestText(text, source);
        return Map.of(
                "status", "success",
                "message", "Document ingested",
                "source", source
        );
    }

    /**
     * Ingest multiple documents at once.
     */
    @PostMapping("/ingest/batch")
    public Map<String, Object> ingestBatch(@RequestBody List<String> documents) {
        ragService.ingestDocuments(documents);
        return Map.of(
                "status", "success",
                "message", "Ingested " + documents.size() + " documents"
        );
    }

    /**
     * Test retrieval without going through the full agent loop.
     */
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String query,
                                      @RequestParam(defaultValue = "4") int maxResults) {
        String context = ragService.retrieveContext(query, maxResults);
        return Map.of(
                "query", query,
                "context", context
        );
    }
}
