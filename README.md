# LangChain4j for LLM Application Development

Java port of the deeplearning.ai course
[LangChain for LLM Application Development](https://www.deeplearning.ai/courses/langchain),
using **LangChain4j** with a **Hugging Face** backend instead of the original
Python/OpenAI stack.

## Project Overview

This is the Java counterpart of the Python project. It is structured by course
chapter. **Chapters 1 through 5 are fully ported**; remaining chapters are
scaffolded and will be added incrementally.

## Chapters

### Chapter 1 — Models, Prompts and Parsers ✅

| Class | Description |
|-------|-------------|
| `CallLlmModelClassic` | Direct LLM calls using `HuggingFaceChatModel` (no prompt template) |
| `CallLlmModelLangChain` | Same use case rewritten with `PromptTemplate` and input variables |
| `ExtractReviewInfo` | Structured JSON extraction from a product review (parsed with Jackson) |

### Chapter 2 — Memory ✅

| Class | Description |
|-------|-------------|
| `ConversationBufferMemory` | Retains the full conversation history; simple but can grow unbounded (custom `ChatMemory`, no LangChain4j built-in) |
| `ConversationBufferWindowMemory` | `MessageWindowChatMemory` keeps only the last `k` messages; prevents unbounded growth |
| `ConversationTokenBufferMemory` | `TokenWindowChatMemory` with a custom word-count `TokenCountEstimator`; keeps messages up to a token budget |
| `ConversationSummaryMemory` | Summarizes old messages with the LLM itself; condenses history while preserving key information (custom class, no LangChain4j built-in) |

### Chapter 3 — Chains ✅

| Class | Description |
|-------|-------------|
| `LlmChain` | Basic building block: a reusable `PromptTemplate` + `ChatModel` pair (custom class — LangChain4j has no dedicated `LLMChain` type) |
| `SimpleSequentialChain` | Two chains run in sequence; the single string output of one feeds into the next |
| `SequentialChain` | Multi-step pipeline with named inputs/outputs (translate → summarize / detect language → follow-up reply) |
| `RouterChain` | Classifies a question's subject and routes it to the matching sub-chain (physics, math, history, computer science), or a default chain |

### Chapter 4 — Q&A over Documents (RAG) ✅

| Class | Description |
|-------|-------------|
| `QaOverDocuments` | Loads a product catalog CSV, embeds each row locally with `AllMiniLmL6V2EmbeddingModel` (in-process ONNX, no API call), indexes them in an `InMemoryEmbeddingStore`, retrieves the top matches for a question, and "stuffs" them into a prompt for the chat model |

### Chapter 5 — Evaluation ✅

| Class | Description |
|-------|-------------|
| `QaManualEvaluation` | Runs 5 hard-coded `(query, expected answer)` pairs through the chapter 4 RAG pipeline, scores each prediction with a keyword-overlap heuristic, and prints a CORRECT/INCORRECT verdict plus a final score |
| `QaLlmEvaluation` | Generates `(query, answer)` ground-truth pairs directly from the product catalog with the LLM itself (LangChain4j has no `QAGenerateChain`), runs each through the RAG pipeline, then has the LLM grade each prediction against the expected answer (no built-in `QAEvalChain` either) — LLM-as-judge instead of keyword matching |

### Planned

- Chapter 6 — Agents

## Setup

### Prerequisites

- Java 17+
- Maven 3.9+
- Hugging Face API token

### Configure credentials

Copy `.env.example` to `.env` and fill in your token:

```
HF_TOKEN=your_token_here
HF_MODEL=Qwen/Qwen2.5-0.5B-Instruct
```

Defaults to a small 0.5B instruct model to minimize Inference Providers cost
(this is a test/demo project). Swap in a larger model (e.g.
`Qwen/Qwen2.5-7B-Instruct`) if you need better answer quality.

### Build

This project requires JDK 17+ (uses text blocks). If your `JAVA_HOME` points to
an older JDK (check with `java -version`), point it at a JDK 17+ installation
for the build session before running Maven:

**Windows (PowerShell)**

```powershell
$env:JAVA_HOME="<path-to-your-jdk-17-or-newer>"
mvn -q clean compile
```

**macOS / Linux (bash/zsh)**

```bash
export JAVA_HOME=<path-to-your-jdk-17-or-newer>
mvn -q clean compile
```

If you don't have a JDK 17+ installed, download one from
[Eclipse Temurin](https://adoptium.net/) or use a version manager such as
`sdkman` (`sdk install java 21-tem`) or `jenv`.

### Run a chapter

**Windows (PowerShell)** — quote the whole `-D` argument (not just the value),
otherwise PowerShell mis-splits it at the `=` and Maven reports
`Unknown lifecycle phase`:

```powershell
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter1.CallLlmModelClassic"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter1.CallLlmModelLangChain"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter1.ExtractReviewInfo"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter2.ConversationBufferMemory"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter2.ConversationBufferWindowMemory"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter2.ConversationTokenBufferMemory"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter2.ConversationSummaryMemory"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter3.LlmChain"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter3.SimpleSequentialChain"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter3.SequentialChain"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter3.RouterChain"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter4.QaOverDocuments"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter5.QaManualEvaluation"
mvn -q exec:java "-Dexec.mainClass=com.coursera.langchain.chapter5.QaLlmEvaluation"
```

**macOS / Linux (bash/zsh)**

```bash
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter1.CallLlmModelClassic"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter1.CallLlmModelLangChain"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter1.ExtractReviewInfo"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter2.ConversationBufferMemory"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter2.ConversationBufferWindowMemory"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter2.ConversationTokenBufferMemory"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter2.ConversationSummaryMemory"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter3.LlmChain"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter3.SimpleSequentialChain"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter3.SequentialChain"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter3.RouterChain"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter4.QaOverDocuments"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter5.QaManualEvaluation"
mvn -q exec:java -Dexec.mainClass="com.coursera.langchain.chapter5.QaLlmEvaluation"
```

## Key Concepts

### Chapter 1 — Models, Prompts and Parsers

- Direct vs. template-driven LLM calls
- `PromptTemplate` with `{{variable}}` placeholders
- JSON output parsing for structured extraction
- Shared `HfConfig` reads `HF_TOKEN` / `HF_MODEL` from `.env`

### Chapter 2 — Memory

- `ChatMemory` interface as the common contract for all memory strategies
- `MessageWindowChatMemory` — fixed-size sliding window of messages
- `TokenWindowChatMemory` — token-budget-based retention, paired with a custom
  `TokenCountEstimator` (word-count approximation)
- Custom `ChatMemory` implementation for unbounded buffer memory (no LangChain4j
  built-in for this)
- Custom LLM-powered summarization memory that progressively condenses history
  via `PromptTemplate`
- Comparing memory strategies for long conversations

### Chapter 3 — Chains

- `LlmChain` — reusable `PromptTemplate` + `ChatModel` wrapper, the LangChain4j
  equivalent of Python's `prompt | llm | StrOutputParser()` composition
- `SimpleSequentialChain` — linear pipeline; a single string is passed between
  steps
- `SequentialChain` — multi-variable pipeline; named inputs/outputs flow
  between steps
- `RouterChain` — conditional routing; a classifier chain selects the best
  sub-chain for the given input, falling back to a default chain
- Composing multiple LLM calls into pipelines without a dedicated "chain"
  abstraction (LangChain4j favors plain Java composition over LCEL operators)

### Chapter 4 — Q&A over Documents (RAG)

- Retrieval-Augmented Generation: loading a product catalog, embedding it into
  a vector store, and answering questions grounded in that data
- Hand-rolled quoted-CSV parser reading `data/outdoor_clothing.csv` from the
  classpath (no third-party CSV loader dependency)
- `AllMiniLmL6V2EmbeddingModel` — local, in-process ONNX embeddings (no API
  call), matching Python's local `sentence-transformers/all-MiniLM-L6-v2`
- `InMemoryEmbeddingStore<TextSegment>` — lightweight, in-process vector store
  (from `langchain4j-core`, no extra dependency needed)
- `EmbeddingSearchRequest` / `store.search(...)` — retrieves the top-K most
  relevant rows for a question
- "Stuffing" the retrieved rows into a `PromptTemplate` for the chat model,
  the LangChain4j equivalent of `chain_type="stuff"`; LangChain4j has no
  `VectorstoreIndexCreator`/`index.query(...)` convenience wrapper, so the
  loader → embed → store → retrieve → prompt steps are wired explicitly

### Chapter 5 — Evaluation

- Manual evaluation — hard-coded `(query, expected answer)` pairs serve as a
  ground-truth test set
- Keyword-overlap scoring — checks whether key words from the expected answer
  appear in the model's response
- LLM-assisted evaluation — the LLM itself generates the ground-truth
  `(query, answer)` pairs from the product catalog (no built-in
  `QAGenerateChain` in LangChain4j, so it's a plain `PromptTemplate` asking
  for JSON output) and then grades each prediction against the expected
  answer (no built-in `QAEvalChain` either — another `PromptTemplate` asking
  for a CORRECT/INCORRECT verdict)
- Reuses the chapter 4 RAG pipeline (`QaOverDocuments.loadCsvAsSegments`,
  `buildStore`, `answer`) instead of duplicating the loader/embedding/store
  wiring
- Cost control: this project targets test/demo usage only, so
  `HfConfig` caps completions at `HF_MAX_TOKENS` (default 256, override in
  `.env`), and `QaLlmEvaluation` — the priciest chapter at 3 LLM calls per
  document (generate + predict + grade) — defaults to a small `SAMPLE_SIZE`
  of 2 documents (override with `-DsampleSize=N`)
