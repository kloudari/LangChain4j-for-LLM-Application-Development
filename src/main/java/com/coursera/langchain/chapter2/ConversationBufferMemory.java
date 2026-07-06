package com.coursera.langchain.chapter2;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Chapter 2 — a minimal {@link ChatMemory} that retains the full conversation
 * history with no limit. Simple, but the history can grow unbounded for long
 * conversations. LangChain4j has no built-in "unbounded buffer" memory (it
 * nudges you toward windowed/token-limited memory instead), so this is a
 * small custom implementation — the counterpart of Python's
 * {@code ConversationBufferMemory}.
 */
public class ConversationBufferMemory implements ChatMemory {

    private final List<ChatMessage> history = new ArrayList<>();

    @Override
    public Object id() {
        return "default";
    }

    @Override
    public void add(ChatMessage message) {
        history.add(message);
    }

    @Override
    public List<ChatMessage> messages() {
        return history;
    }

    @Override
    public void clear() {
        history.clear();
    }

    private static String chat(ChatModel chatModel, ChatMemory memory, String userInput) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(ChatMessages.SYSTEM_PROMPT));
        messages.addAll(memory.messages());
        messages.add(UserMessage.from(userInput));

        System.out.println("\n> Entering new ConversationChain chain...");
        System.out.println("Prompt after formatting:");
        ChatMessages.printMessages(messages);

        ChatResponse response = chatModel.chat(messages);
        String aiText = response.aiMessage().text();
        System.out.println("AI: " + aiText);
        System.out.println("\n> Finished chain.");

        // Save the full exchange; the buffer keeps growing with every turn.
        memory.add(UserMessage.from(userInput));
        memory.add(AiMessage.from(aiText));
        return aiText;
    }

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();
        ConversationBufferMemory memory = new ConversationBufferMemory();

        chat(chatModel, memory, "Hi, my name is Karim");
        chat(chatModel, memory, "What is 1+1?");
        chat(chatModel, memory, "What is my name?");

        System.out.println();
        ChatMessages.printMessages(memory.messages());

        System.out.println();
        System.out.println("{history=" + memory.messages() + "}");

        // A small standalone illustration of the memory API, independent of
        // the LLM calls above (mirrors the demo at the end of the Python script).
        ConversationBufferMemory demoMemory = new ConversationBufferMemory();
        demoMemory.add(UserMessage.from("Hi"));
        demoMemory.add(AiMessage.from("What's up"));
        ChatMessages.printMessages(demoMemory.messages());
        System.out.println("{history=" + demoMemory.messages() + "}");

        demoMemory.add(UserMessage.from("Not much, just hanging"));
        demoMemory.add(AiMessage.from("Cool"));
        System.out.println("{history=" + demoMemory.messages() + "}");
    }
}
