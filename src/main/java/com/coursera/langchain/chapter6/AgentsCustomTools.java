package com.coursera.langchain.chapter6;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

import java.util.List;

/**
 * Chapter 6 — Custom Tools Agent: registers six hand-crafted tools (today's
 * date, calculator, text stats, temperature conversion x2, string
 * reversal) and runs one demo query per tool.
 *
 * <p>Ported from {@code agents_custom_tools.py}.
 */
public class AgentsCustomTools {

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();

        ToolAgent agent = AiServices.builder(ToolAgent.class)
                .chatModel(chatModel)
                .tools(new CalculatorTool(), new CustomTools())
                .build();

        List<String> questions = List.of(
                "What's the date today?",
                "What is the square root of 256 plus 10 percent of 500?",
                "How many words are in this sentence: The quick brown fox jumps over the lazy dog",
                "Convert 37 degrees Celsius to Fahrenheit.",
                "Convert 98.6 Fahrenheit to Celsius.",
                "Reverse the string 'LangChain'.");

        System.out.println("=".repeat(70));
        System.out.println("CUSTOM TOOLS AGENT");
        System.out.println("=".repeat(70));
        for (String question : questions) {
            System.out.println("-".repeat(70));
            System.out.println("Q: " + question);
            System.out.println("A: " + agent.chat(question));
        }
    }
}
