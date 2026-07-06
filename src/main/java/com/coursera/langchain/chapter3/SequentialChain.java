package com.coursera.langchain.chapter3;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chapter 3 — SequentialChain: a multi-step pipeline with named inputs and
 * outputs flowing between steps (translate → summarize / detect language →
 * follow-up reply). Java counterpart of Python's {@code SequentialChain}
 * (built there with LCEL {@code RunnableParallel} steps); here the steps are
 * simply run in order, threading named values through a result map.
 */
public class SequentialChain {

    private static final ChatModel chatModel = HfConfig.chatModel();

    // Chain 1: translate the review to English
    private static final LlmChain translateChain = new LlmChain(
            PromptTemplate.from("Translate the following review to English:\n\n{{review}}"),
            chatModel);

    // Chain 2: summarize the English review in 1 sentence
    private static final LlmChain summarizeChain = new LlmChain(
            PromptTemplate.from("Summarize the following review in 1 sentence:\n\n{{english_review}}"),
            chatModel);

    // Chain 3: detect the language of the original review
    private static final LlmChain detectLanguageChain = new LlmChain(
            PromptTemplate.from(
                    "What language is the following review written in? "
                            + "Reply with only the language name, nothing else.\n\n{{review}}"),
            chatModel);

    // Chain 4: write a follow-up reply in the detected language
    private static final LlmChain followupChain = new LlmChain(
            PromptTemplate.from(
                    "You are a customer service agent. Write a short, polite reply to the customer "
                            + "based on the following review summary. Write your reply in {{language}}. "
                            + "Reply with only the response message, nothing else.\n\n"
                            + "Review summary: {{summary}}"),
            chatModel);

    /** Runs the full pipeline over a single review, returning all named outputs. */
    private static Map<String, String> run(String review) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("review", review);

        String englishReview = translateChain.invoke(Map.of("review", review)).strip();
        result.put("english_review", englishReview);

        String summary = summarizeChain.invoke(Map.of("english_review", englishReview)).strip();
        result.put("summary", summary);

        String language = detectLanguageChain.invoke(Map.of("review", review)).strip();
        result.put("language", language);

        String followup = followupChain.invoke(Map.of("summary", summary, "language", language)).strip();
        result.put("followup_message", followup);

        return result;
    }

    public static void main(String[] args) {
        List<String> reviews = List.of(
                "Je trouve que ce produit est vraiment excellent. "
                        + "La qualité est au rendez-vous et la livraison a été très rapide. "
                        + "Je le recommande vivement à tous !",
                "Ce produit est une véritable déception. "
                        + "La qualité est médiocre, il s'est cassé après seulement deux jours d'utilisation. "
                        + "Le service client n'a pas répondu à mes messages. "
                        + "Je ne recommande absolument pas cet achat.");

        int i = 1;
        for (String review : reviews) {
            System.out.println("=== Review " + i + " ===");
            Map<String, String> result = run(review);

            System.out.println("Original review : " + review);
            System.out.println();
            System.out.println("English review  : " + result.get("english_review"));
            System.out.println();
            System.out.println("Summary         : " + result.get("summary"));
            System.out.println();
            System.out.println("Language        : " + result.get("language"));
            System.out.println();
            System.out.println("Follow-up       : " + result.get("followup_message"));
            System.out.println();
            i++;
        }
    }
}
