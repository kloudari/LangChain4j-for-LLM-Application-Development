package com.coursera.langchain.chapter3;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

/**
 * Chapter 3 — SimpleSequentialChain: two chains wired in sequence where a
 * single string flows from the output of one chain into the input of the
 * next. Java counterpart of Python's {@code SimpleSequentialChain}.
 */
public class SimpleSequentialChain {

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();

        // Chain 1: generate a company name from a product
        LlmChain chain1 = new LlmChain(
                PromptTemplate.from(
                        "What is the best name to describe a company that makes {{product}}? "
                                + "Reply with only the company name, nothing else."),
                chatModel);

        // Chain 2: generate a 20-word description for the company name
        LlmChain chain2 = new LlmChain(
                PromptTemplate.from("Write a 20 words description for the following company: {{company_name}}"),
                chatModel);

        String product = "Queen Size Sheet Set";

        String companyName = chain1.invoke(Map.of("product", product)).strip();
        System.out.println(companyName);

        String description = chain2.invoke(Map.of("company_name", companyName)).strip();
        System.out.println(description);
    }
}
