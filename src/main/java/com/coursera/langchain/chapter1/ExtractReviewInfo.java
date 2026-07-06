package com.coursera.langchain.chapter1;

import com.coursera.langchain.HfConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

/**
 * Chapter 1 — structured extraction from a product review, parsing the model
 * output as JSON.
 */
public class ExtractReviewInfo {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final PromptTemplate REVIEW_TEMPLATE = PromptTemplate.from("""
            For the following text, extract the following information:

            gift: Was the item purchased as a gift for someone else? \
            Answer True if yes, False if not or unknown.

            delivery_days: How many days did it take for the product \
            to arrive? If this information is not found, output -1.

            price_value: Extract any sentences about the value or price, \
            and output them as a comma separated list.

            Format the output as JSON with the following keys:
            gift
            delivery_days
            price_value

            Respond with JSON only, no markdown fences.
            text: {{text}}""");

    public static void main(String[] args) throws Exception {
        ChatModel chatModel = HfConfig.chatModel();

        String customerReview = """
                This leaf blower is pretty amazing. It has four settings: \
                candle blower, gentle breeze, windy city, and tornado. \
                It arrived in two days, just in time for my wife's \
                anniversary present. I think my wife liked it so much she was speechless. \
                So far I've been the only one using it, and I've been \
                using it every other morning to clear the leaves on our lawn. \
                It's slightly more expensive than the other leaf blowers \
                out there, but I think it's worth it for the extra features.""";

        Prompt prompt = REVIEW_TEMPLATE.apply(Map.of("text", customerReview));
        String raw = chatModel.chat(prompt.text());
        System.out.println(raw);

        JsonNode json = MAPPER.readTree(stripFences(raw));
        System.out.println(json);
        System.out.println(json.path("gift").asText());
    }

    private static String stripFences(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```(json)?", "").replaceFirst("```$", "").trim();
        }
        return t;
    }
}
