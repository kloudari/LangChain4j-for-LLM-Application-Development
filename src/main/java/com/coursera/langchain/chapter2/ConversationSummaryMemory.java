package com.coursera.langchain.chapter2;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * Chapter 2 — a running LLM-generated summary of the conversation so far.
 * Instead of storing raw messages, after each exchange it asks the LLM to
 * fold the latest turn into the previous summary. LangChain4j has no
 * built-in summary memory, so this is a small custom class — the
 * counterpart of Python's {@code ConversationSummaryMemory}.
 */
public class ConversationSummaryMemory {

    private static final PromptTemplate SUMMARY_UPDATE_TEMPLATE = PromptTemplate.from("""
            Progressively summarize the lines of conversation provided, \
            adding onto the previous summary returning a new summary.

            EXAMPLE
            Current summary:
            The human asks what the AI thinks of artificial intelligence. \
            The AI thinks artificial intelligence is a force for good.

            New lines of conversation:
            Human: Why do you think artificial intelligence is a force for good?
            AI: Because artificial intelligence will help humans reach their full potential.

            New summary:
            The human asks what the AI thinks of artificial intelligence. \
            The AI thinks artificial intelligence is a force for good because it will help \
            humans reach their full potential.
            END OF EXAMPLE

            Current summary:
            {{summary}}

            New lines of conversation:
            Human: {{human_msg}}
            AI: {{ai_msg}}

            New summary:""");

    private final ChatModel chatModel;
    private String summary = "";

    public ConversationSummaryMemory(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /** Updates the running summary with the latest human-AI exchange. */
    public void saveContext(String humanMsg, String aiMsg) {
        Prompt prompt = SUMMARY_UPDATE_TEMPLATE.apply(
                Map.of("summary", summary, "human_msg", humanMsg, "ai_msg", aiMsg));
        summary = chatModel.chat(prompt.text()).strip();
    }

    public String getSummary() {
        return summary;
    }

    private static String chat(ChatModel chatModel, ConversationSummaryMemory memory, String userInput) {
        String systemContent = ChatMessages.SYSTEM_PROMPT
                + "\n\nCurrent conversation summary:\n" + memory.getSummary();

        List<ChatMessage> messages = List.of(
                SystemMessage.from(systemContent),
                UserMessage.from(userInput));

        System.out.println("\n> Entering new ConversationChain chain...");
        System.out.println("Prompt after formatting:");
        ChatMessages.printMessages(messages);

        ChatResponse response = chatModel.chat(messages);
        String aiText = response.aiMessage().text();
        System.out.println("AI: " + aiText);
        System.out.println("\n> Finished chain.");

        memory.saveContext(userInput, aiText);
        System.out.println("\n[Memory State (summary)]:\n" + memory.getSummary());

        return aiText;
    }

    public static void main(String[] args) {
        ChatModel chatModel = HfConfig.chatModel();
        ConversationSummaryMemory memory = new ConversationSummaryMemory(chatModel);

        System.out.println("=".repeat(70));
        System.out.println("ConversationSummaryMemory Demo");
        System.out.println("Keeps a running LLM-generated summary instead of raw message history");
        System.out.println("=".repeat(70));

        chat(chatModel, memory, "Hi, my name is Karim and I work in machine learning.");
        chat(chatModel, memory, "What are interesting topics in machine learning?");
        System.out.println("\n>>> Turn 3: Testing memory via summary");
        chat(chatModel, memory, "What is my name and what do I work on?");
    }
}
