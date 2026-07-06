package com.coursera.langchain.chapter3;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

/**
 * Chapter 3 — LLMChain: the most basic chain, wiring a {@link PromptTemplate}
 * to a {@link ChatModel}. LangChain4j has no dedicated {@code LLMChain} type
 * (a chat model already returns plain text, acting as its own "output
 * parser"), so this is a small reusable wrapper mirroring Python's
 * {@code prompt | llm | StrOutputParser()} composition. It is reused by the
 * other chapter 3 classes to build multi-step pipelines.
 */
public class LlmChain {

    private final PromptTemplate promptTemplate;
    private final ChatModel chatModel;

    public LlmChain(PromptTemplate promptTemplate, ChatModel chatModel) {
        this.promptTemplate = promptTemplate;
        this.chatModel = chatModel;
    }

    /** Fills the template with the given variables and runs it through the chat model. */
    public String invoke(Map<String, Object> variables) {
        Prompt prompt = promptTemplate.apply(variables);
        return chatModel.chat(prompt.text());
    }

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();

        LlmChain chain = new LlmChain(
                PromptTemplate.from("What is the best name to describe a company that makes {{product}}?"),
                chatModel);

        String product = "Queen Size Sheet Set";
        String result = chain.invoke(Map.of("product", product));
        System.out.println(result);
    }
}
