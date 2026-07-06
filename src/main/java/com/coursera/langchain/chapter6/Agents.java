package com.coursera.langchain.chapter6;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Chapter 6 — Agents: an LLM equipped with a calculator tool and a
 * Wikipedia lookup tool, deciding for itself whether (and which) tool to
 * call to answer a question. Java counterpart of Python's
 * {@code create_agent} + LangGraph ReAct agent; LangChain4j's
 * {@code AiServices} drives the reasoning/tool-calling loop internally, so
 * no explicit Thought/Action/Observation wiring is needed.
 *
 * <p>Ported from {@code agents.py}.
 */
public class Agents {

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();

        ToolAgent agent = AiServices.builder(ToolAgent.class)
                .chatModel(chatModel)
                .tools(new CalculatorTool(), new WikipediaTool())
                .build();

        System.out.println("=".repeat(70));
        System.out.println("AGENT \u2014 Math question");
        System.out.println("=".repeat(70));
        System.out.println(agent.chat("What is 25% of 300?"));

        System.out.println("=".repeat(70));
        System.out.println("AGENT \u2014 Wikipedia question");
        System.out.println("=".repeat(70));
        System.out.println(agent.chat(
                "Tom M. Mitchell is an American computer scientist and the Founders "
                        + "University Professor at Carnegie Mellon University (CMU). "
                        + "What book did he write?"));
    }
}
