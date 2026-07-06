package com.coursera.langchain.chapter2;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Chapter 2 — {@link MessageWindowChatMemory} keeps only the last {@code k}
 * messages, which prevents the conversation history from growing unbounded.
 * The LangChain4j counterpart of Python's {@code ConversationBufferWindowMemory}.
 */
public class ConversationBufferWindowMemory {

    private static final int WINDOW_SIZE = 2; // last 2 messages == 1 human/AI exchange

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

        // Messages beyond the window are pruned automatically by the memory.
        memory.add(UserMessage.from(userInput));
        memory.add(AiMessage.from(aiText));
        System.out.println("\n[Memory State (k=" + WINDOW_SIZE + ", max " + WINDOW_SIZE + " messages)]:");
        System.out.println(ChatMessages.bufferString(memory.messages()));

        return aiText;
    }

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();
        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(WINDOW_SIZE);

        System.out.println("=".repeat(70));
        System.out.println("MessageWindowChatMemory Demo (k=" + WINDOW_SIZE + ")");
        System.out.println("Keeps only the last " + WINDOW_SIZE + " messages (1 human-AI exchange)");
        System.out.println("=".repeat(70));

        chat(chatModel, memory, "Hi, my name is Karim");
        chat(chatModel, memory, "What is 1+1?");
        System.out.println("\n>>> Turn 3: Testing memory (should not remember my name)");
        chat(chatModel, memory, "What is my name?");
        System.out.println("\nNote: The first exchange is no longer in memory due to window size k=" + WINDOW_SIZE);
    }
}
