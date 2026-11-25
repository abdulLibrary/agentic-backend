# Agentic Backend v1.3 – Multi Vector Store + Multi Model Support

Advanced Agentic AI backend with **In-Memory / Qdrant / PGVector** and support for **OpenAI + Ollama + OpenAI-compatible APIs**.

---

### Model Provider Support

| Provider                    | Status | How to enable                          |
|----------------------------|--------|----------------------------------------|
| **OpenAI**                 | ✅     | Default (`AI_PROVIDER=openai`)         |
| **Ollama (Local)**         | ✅     | `AI_PROVIDER=ollama`                   |
| **OpenAI-compatible APIs** | ✅*    | Use OpenAI provider + custom base-url  |

> *OpenAI-compatible providers (Groq, Together, Fireworks, DeepSeek, LM Studio, LocalAI, vLLM, OpenRouter, etc.) work by pointing the OpenAI client to their endpoint.

---

### 1. Using OpenAI (Default)

```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=sk-your-key-here
./mvnw spring-boot:run
```

---

### 2. Using Local Ollama

```bash
# Start Ollama and pull a model
ollama pull llama3.2
# or: ollama pull mistral / phi3 / qwen2.5 / etc.

export AI_PROVIDER=ollama
export OLLAMA_MODEL=llama3.2
export OLLAMA_BASE_URL=http://localhost:11434

./mvnw spring-boot:run
```

---

### 3. Using any OpenAI-compatible API

You can use providers that expose an OpenAI-compatible endpoint by setting the base URL.

**Examples:**

#### Groq
```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=gsk-your-groq-key
export OPENAI_BASE_URL=https://api.groq.com/openai/v1
export OPENAI_MODEL=llama-3.3-70b-versatile
```

#### Together.ai
```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=your-together-key
export OPENAI_BASE_URL=https://api.together.xyz/v1
export OPENAI_MODEL=meta-llama/Llama-3-70b-chat-hf
```

#### LM Studio / LocalAI / vLLM (local)
```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=not-needed
export OPENAI_BASE_URL=http://localhost:1234/v1
export OPENAI_MODEL=local-model
```

> Note: To fully support custom `base-url`, a small code update is recommended (see below). Currently Ollama has first-class support. OpenAI-compatible endpoints can be enabled by updating `LangChainConfig`.

---

### Vector Store Options

| Type       | Config Value | Use Case                        |
|------------|--------------|---------------------------------|
| In-Memory  | `memory`     | Demo / Interview / Local testing |
| Qdrant     | `qdrant`     | Production / Serious local      |
| PGVector   | `pgvector`   | Already using PostgreSQL        |

#### Switch Vector Store

```bash
# In-Memory (default)
export VECTOR_STORE_TYPE=memory

# Qdrant
docker run -d -p 6333:6333 --name qdrant qdrant/qdrant
export VECTOR_STORE_TYPE=qdrant

# PGVector
export VECTOR_STORE_TYPE=pgvector
export PGVECTOR_HOST=localhost
export PGVECTOR_DB=agentic
export PGVECTOR_USER=postgres
export PGVECTOR_PASSWORD=postgres
```

---

### Full Feature List

| Feature                              | Status |
|--------------------------------------|--------|
| Dynamic Supervisor                   | ✅     |
| Confidence + Human-in-the-loop       | ✅     |
| Persistent conversation memory       | ✅     |
| WebSocket + SSE                      | ✅     |
| **Ollama (local models)**            | ✅     |
| **OpenAI**                           | ✅     |
| Observability                        | ✅     |
| RAG Knowledge Agent                  | ✅     |
| In-Memory Vector Store               | ✅     |
| Qdrant support                       | ✅     |
| PGVector support                     | ✅     |
| Document Ingestion API               | ✅     |
| Chat UI                              | ✅     |

---

### Quick Start Examples

**OpenAI + In-Memory (simplest)**
```bash
export OPENAI_API_KEY=sk-your-key
./mvnw spring-boot:run
```

**Ollama (fully local)**
```bash
ollama pull llama3.2
export AI_PROVIDER=ollama
export OLLAMA_MODEL=llama3.2
./mvnw spring-boot:run
```

**OpenAI + Qdrant**
```bash
docker run -d -p 6333:6333 qdrant/qdrant
export OPENAI_API_KEY=sk-your-key
export VECTOR_STORE_TYPE=qdrant
./mvnw spring-boot:run
```

Open the UI → http://localhost:8080

---

### Configuration Reference (`application.yml`)

```yaml
ai:
  provider: openai          # openai | ollama

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4o-mini
  # base-url: https://api.groq.com/openai/v1   # for compatible providers

ollama:
  base-url: http://localhost:11434
  model: llama3.2

vector-store:
  type: memory              # memory | qdrant | pgvector

  qdrant:
    host: localhost
    port: 6333
    collection: knowledge-base

  pgvector:
    host: localhost
    port: 5432
    database: agentic
    user: postgres
    password: postgres
    table: embeddings
    dimension: 384
```

---

### Version

**1.3.0** – Multi vector store + Ollama + OpenAI support
