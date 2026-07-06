package com.coursera.langchain.chapter6;

/**
 * Minimal LangChain4j AI Service: a single chat method that the LLM
 * fulfils, calling any {@code @Tool}-annotated methods bound to it as
 * needed. LangChain4j's {@code AiServices} generates the implementation at
 * runtime and drives the reasoning/tool-calling loop internally — the Java
 * counterpart of Python's {@code create_agent} / {@code AgentExecutor}.
 *
 * <p>Shared by all three chapter 6 agent demos ({@link Agents},
 * {@link AgentsPython}, {@link AgentsCustomTools}).
 */
interface ToolAgent {

    String chat(String userMessage);
}
