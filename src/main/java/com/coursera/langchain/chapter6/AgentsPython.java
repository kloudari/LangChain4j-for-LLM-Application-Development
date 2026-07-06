package com.coursera.langchain.chapter6;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Chapter 6 — "Python Agent" equivalent: the LLM is given a code-execution
 * tool and asked to sort a list of customers by last name then first name.
 * Since the JVM has no Python interpreter, {@link JavaCodeExecutorTool}
 * lets the LLM write Java instead of Python; the reasoning/tool-calling
 * loop is otherwise the same idea as the original.
 *
 * <p>Ported from {@code agents_python.py}.
 */
public class AgentsPython {

    private static final String[][] CUSTOMERS = {
            {"Harrison", "Chase"},
            {"Lang", "Chain"},
            {"Dolly", "Too"},
            {"Elle", "Elem"},
            {"Geoff", "Fusion"},
            {"Trance", "Former"},
            {"Jen", "Ayai"},
    };

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();

        ToolAgent agent = AiServices.builder(ToolAgent.class)
                .chatModel(chatModel)
                .tools(new JavaCodeExecutorTool())
                .build();

        String customerListLiteral = toJavaListLiteral(CUSTOMERS);

        System.out.println("=".repeat(70));
        System.out.println("PYTHON AGENT (Java code) \u2014 Sort customers by last name then first name");
        System.out.println("=".repeat(70));
        System.out.println(agent.chat(
                "Using the executeJavaCode tool, write and run Java code that sorts these customers "
                        + "by last name and then first name, and prints the sorted output: "
                        + customerListLiteral));
    }

    private static String toJavaListLiteral(String[][] customers) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < customers.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("[\"").append(customers[i][0]).append("\", \"").append(customers[i][1]).append("\"]");
        }
        return sb.append("]").toString();
    }
}
