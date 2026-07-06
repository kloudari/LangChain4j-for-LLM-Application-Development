package com.coursera.langchain.chapter1;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

/**
 * Chapter 1 — same use case rewritten with a {@link PromptTemplate} and input
 * variables (the LangChain4j equivalent of {@code ChatPromptTemplate}).
 */
public class CallLlmModelLangChain {

    private static final ChatModel chatModel = HfConfig.chatModel();

    private static final PromptTemplate PROMPT_TEMPLATE = PromptTemplate.from(
            "Translate (don't answer) the text that is delimited by triple backticks\n"
                    + "into a style that is {{style}}.\n"
                    + "text: ```{{customer_email}}```");

    public static void main(String[] args) {
        String customerEmail = """
                Arrr, I be fuming that me blender lid \
                flew off and splattered me kitchen walls \
                with smoothie! And to make matters worse, \
                the warranty don't cover the cost of \
                cleaning up me kitchen. I need yer help \
                right now, matey!""";

        String customerStyle = "American English in a calm and respectful tone";

        Prompt customerPrompt = PROMPT_TEMPLATE.apply(
                Map.of("style", customerStyle, "customer_email", customerEmail));
        System.out.println(chatModel.chat(customerPrompt.text()));

        String serviceReply = """
                Hey there customer, the warranty does not cover \
                cleaning expenses for your kitchen because it's your fault \
                that you misused your blender by forgetting to put the lid on \
                before starting the blender. Tough luck! See ya!""";

        String servicePirateStyle = "a polite tone that speaks in English Pirate";

        Prompt servicePrompt = PROMPT_TEMPLATE.apply(
                Map.of("style", servicePirateStyle, "customer_email", serviceReply));
        System.out.println(chatModel.chat(servicePrompt.text()));
    }
}
