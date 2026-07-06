package com.coursera.langchain.chapter2;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Chapter 2 — {@link TokenWindowChatMemory} keeps messages up to a token
 * budget, evicting the oldest ones once the limit is exceeded. The
 * LangChain4j counterpart of Python's {@code ConversationTokenBufferMemory}.
 *
 * <p>Token count is approximated as word count (1 word ≈ 1 token), same as
 * the original Python script, to keep the demo self-contained and model
 * agnostic.
 */
public class ConversationTokenBufferMemory {

    private static final int MAX_TOKEN_LIMIT = 80;

    /** Approximates token count as word count (1 word ≈ 1 token). */
    private static final class WordCountTokenEstimator implements TokenCountEstimator {
        @Override
        public int estimateTokenCountInText(String text) {
            return text.isBlank() ? 0 : text.trim().split("\\s+").length;
        }

        @Override
        public int estimateTokenCountInMessage(ChatMessage message) {
            return estimateTokenCountInText(ChatMessages.textOf(message));
        }

        @Override
        public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
            int total = 0;
            for (ChatMessage message : messages) {
                total += estimateTokenCountInMessage(message);
            }
            return total;
        }
    }

    private static String chat(ChatModel chatModel, ChatMemory memory, TokenCountEstimator estimator, String userInput) {
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

        // Save the exchange; the memory auto-prunes the oldest messages once
        // the token budget is exceeded.
        memory.add(UserMessage.from(userInput));
        memory.add(AiMessage.from(aiText));
        int tokens = estimator.estimateTokenCountInMessages(memory.messages());
        System.out.println("\n[Memory State (token-limited, max_token_limit=" + MAX_TOKEN_LIMIT + ")]:");
        System.out.println(ChatMessages.bufferString(memory.messages()) + "\n[~" + tokens + " tokens in buffer]");

        return aiText;
    }

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();
        TokenCountEstimator estimator = new WordCountTokenEstimator();
        ChatMemory memory = TokenWindowChatMemory.withMaxTokens(MAX_TOKEN_LIMIT, estimator);

        System.out.println("=".repeat(70));
        System.out.println("TokenWindowChatMemory Demo");
        System.out.println("Keeps as much history as fits in " + MAX_TOKEN_LIMIT + " tokens");
        System.out.println("=".repeat(70));

        chat(chatModel, memory, estimator, "Hi, my name is Karim");
        chat(chatModel, memory, estimator, "What is 1+1?");
        System.out.println("\n>>> Turn 3: Testing memory");
        chat(chatModel, memory, estimator, "What is my name?");
        System.out.println("\nNote: Old messages are pruned automatically when the token limit is exceeded.");
    }
}
