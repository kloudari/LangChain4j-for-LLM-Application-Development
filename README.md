# LangChain4j for LLM Application Development

Java port of the deeplearning.ai course
[LangChain for LLM Application Development](https://www.deeplearning.ai/courses/langchain),
using **LangChain4j** with a **Hugging Face** backend instead of the original
Python/OpenAI stack.

## Project Overview

This is the Java counterpart of the Python project. It is structured by course
chapter. **Chapters 1 and 2 are fully ported**; remaining chapters are
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

### Planned

- Chapter 3 — Chains
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
