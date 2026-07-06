package com.coursera.langchain.chapter2;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;

/**
 * Chapter 2 — small shared helpers for formatting {@link ChatMessage}s,
 * used by the different memory demos (the LangChain4j counterpart of the
 * Python scripts' shared {@code read_key.py} import).
 */
final class ChatMessages {

    static final String SYSTEM_PROMPT =
            "The following is a friendly conversation between a human and an AI. "
                    + "The AI is talkative and provides lots of specific details from its context. "
                    + "If the AI does not know the answer to a question, it truthfully says it does not know.";

    private ChatMessages() {
    }

    static String roleOf(ChatMessage message) {
        return switch (message.type()) {
            case USER -> "Human";
            case AI -> "Ai";
            case SYSTEM -> "System";
            default -> message.type().toString();
        };
    }

    static String textOf(ChatMessage message) {
        if (message instanceof UserMessage userMessage) {
            return userMessage.singleText();
        }
        if (message instanceof AiMessage aiMessage) {
            return aiMessage.text();
        }
        if (message instanceof SystemMessage systemMessage) {
            return systemMessage.text();
        }
        return message.toString();
    }

    static void printMessages(List<ChatMessage> messages) {
        messages.forEach(m -> System.out.println(roleOf(m) + ": " + textOf(m)));
    }

    static String bufferString(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage message : messages) {
            sb.append(roleOf(message)).append(": ").append(textOf(message)).append('\n');
        }
        return sb.toString().stripTrailing();
    }
}
