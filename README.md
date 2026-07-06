# LangChain4j for LLM Application Development

Java port of the deeplearning.ai course
[LangChain for LLM Application Development](https://www.deeplearning.ai/courses/langchain),
using **LangChain4j** with a **Hugging Face** backend instead of the original
Python/OpenAI stack.

## Project Overview

This is the Java counterpart of the Python project. It is structured by course
chapter. **Chapters 1, 2 and 3 are fully ported**; remaining chapters are
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

### Planned

- Chapter 4 — Q&A over Documents (RAG)
- Chapter 5 — Evaluation
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
HF_MODEL=Qwen/Qwen2.5-7B-Instruct
```

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
