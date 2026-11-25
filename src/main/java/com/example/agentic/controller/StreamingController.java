package com.example.agentic.controller;

import com.example.agentic.model.ExecutionResult;
import com.example.agentic.service.DynamicSupervisor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StreamingController {

    private final DynamicSupervisor supervisor;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam String goal,
            @RequestParam(required = false) String conversationId) {

        return Flux.create(sink -> {
            try {
                sink.next(ServerSentEvent.builder("Supervisor started...")
                        .event("status")
                        .build());

                ExecutionResult result = supervisor.execute(goal, conversationId);

                // Send metadata first
                sink.next(ServerSentEvent.builder(
                                "conversationId=" + result.conversationId() +
                                " | status=" + result.status() +
                                " | totalLatency=" + result.totalLatencyMs() + "ms")
                        .event("meta")
                        .build());

                if (result.needsHumanApproval()) {
                    sink.next(ServerSentEvent.builder(
                                    "NEEDS_APPROVAL | confidence=" + result.confidence() +
                                    " | instruction=" + result.pendingInstruction())
                            .event("approval_required")
                            .build());
                } else {
                    // Stream the final result token by token
                    String[] tokens = result.result().split("(?<=\\s)");
                    for (String token : tokens) {
                        sink.next(ServerSentEvent.builder(token)
                                .event("token")
                                .build());
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                sink.next(ServerSentEvent.builder("[DONE]")
                        .event("done")
                        .build());
                sink.complete();

            } catch (Exception e) {
                sink.next(ServerSentEvent.builder("Error: " + e.getMessage())
                        .event("error")
                        .build());
                sink.complete();
            }
        });
    }
}
