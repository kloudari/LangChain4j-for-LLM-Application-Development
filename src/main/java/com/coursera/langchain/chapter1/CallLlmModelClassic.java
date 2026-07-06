package com.coursera.langchain.chapter1;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;

/**
 * Chapter 1 — direct LLM calls with no prompt templates or chains.
 */
public class CallLlmModelClassic {

    private static final ChatModel model = HfConfig.chatModel();

    static String getCompletion(String prompt) {
        return model.chat(prompt);
    }

    public static void main(String[] args) {
        System.out.println(getCompletion("What is 1+1?"));

        String customerEmail = """
                Arrr, I be fuming that me blender lid \
                flew off and splattered me kitchen walls \
                with smoothie! And to make matters worse, \
                the warranty don't cover the cost of \
                cleaning up me kitchen. I need yer help \
                right now, matey!""";

        String style = "American English in a calm and respectful tone";

        String prompt = "Translate (don't answer) the text that is delimited by triple backticks\n"
                + "into a style that is " + style + ".\n"
                + "text: ```" + customerEmail + "```";

        System.out.println(getCompletion(prompt));
    }
}
