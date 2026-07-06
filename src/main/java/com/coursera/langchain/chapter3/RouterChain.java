package com.coursera.langchain.chapter3;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chapter 3 — Router Chain: dynamically routes a question to the most
 * appropriate subject-specific sub-chain (physics, math, history, computer
 * science), falling back to a default chain when no subject matches. Java
 * counterpart of Python's router chain built with LCEL
 * {@code RunnableLambda}/{@code RunnableParallel}.
 */
public class RouterChain {

    private record SubjectInfo(String name, String description, String template) {
    }

    private static final List<SubjectInfo> PROMPT_INFOS = List.of(
            new SubjectInfo("physics", "Good for answering questions about physics", """
                    You are a very smart physics professor. \
                    You are great at answering questions about physics in a concise \
                    and easy to understand manner. \
                    When you don't know the answer to a question you admit that you don't know.

                    Here is a question:
                    {{question}}"""),
            new SubjectInfo("math", "Good for answering math questions", """
                    You are a very good mathematician. \
                    You are great at answering math questions. \
                    You are so good because you are able to break down hard problems into their \
                    component parts, answer the component parts, and then put them together \
                    to answer the broader question.

                    Here is a question:
                    {{question}}"""),
            new SubjectInfo("history", "Good for answering history questions", """
                    You are a very good historian. \
                    You have an excellent knowledge of and understanding of people, events, and \
                    contexts from a range of historical periods. \
                    You have the ability to think, reflect, debate, discuss and evaluate the past. \
                    You have a respect for historical evidence and the ability to make use of it \
                    to support your explanations and judgements.

                    Here is a question:
                    {{question}}"""),
            new SubjectInfo("computer science",
                    "Good for answering computer science and programming questions", """
                    You are a successful computer scientist. \
                    You have a passion for creativity, collaboration, forward-thinking, confidence, \
                    strong problem-solving capabilities, understanding of theories and algorithms, \
                    and excellent communication skills. \
                    You are great at answering coding questions because you know how to solve a \
                    problem by describing the solution in imperative steps that a computer can \
                    easily interpret, while choosing solutions with a good balance between time \
                    and space complexity.

                    Here is a question:
                    {{question}}"""));

    private static final ChatModel chatModel = HfConfig.chatModel();

    private static final Map<String, LlmChain> SUBJECT_CHAINS = buildSubjectChains();

    private static final String SUBJECTS_DESCRIPTION = buildSubjectsDescription();

    // Default chain used when the input doesn't match any known subject
    private static final LlmChain DEFAULT_CHAIN = new LlmChain(
            PromptTemplate.from(
                    "You are a helpful assistant. Answer the following question to the best of your ability."
                            + "\n\n{{question}}"),
            chatModel);

    // Classifier chain — determines which subject the question belongs to
    private static final LlmChain CLASSIFIER_CHAIN = new LlmChain(
            PromptTemplate.from(
                    "Given a user question, classify it into exactly one of the following subjects. "
                            + "Reply with ONLY the subject name from the list, nothing else.\n\n"
                            + "Subjects:\n{{subjects}}\n\n"
                            + "Question: {{question}}"),
            chatModel);

    private static Map<String, LlmChain> buildSubjectChains() {
        Map<String, LlmChain> chains = new LinkedHashMap<>();
        for (SubjectInfo info : PROMPT_INFOS) {
            chains.put(info.name(), new LlmChain(PromptTemplate.from(info.template()), chatModel));
        }
        return chains;
    }

    private static String buildSubjectsDescription() {
        StringBuilder sb = new StringBuilder();
        for (SubjectInfo info : PROMPT_INFOS) {
            sb.append("- ").append(info.name()).append(": ").append(info.description()).append("\n");
        }
        return sb.toString().strip();
    }

    /** Classifies the question, then routes it to the matching subject chain (or the default chain). */
    private static String route(String question) {
        String subject = CLASSIFIER_CHAIN
                .invoke(Map.of("subjects", SUBJECTS_DESCRIPTION, "question", question))
                .strip().toLowerCase();

        for (Map.Entry<String, LlmChain> entry : SUBJECT_CHAINS.entrySet()) {
            if (subject.contains(entry.getKey())) {
                System.out.println("[Router] -> " + entry.getKey());
                return entry.getValue().invoke(Map.of("question", question));
            }
        }

        System.out.println("[Router] -> no match for '" + subject + "', using default chain");
        return DEFAULT_CHAIN.invoke(Map.of("question", question));
    }

    public static void main(String[] args) {
        List<String> questions = List.of(
                "What is black body radiation?",
                "What is 2 + 2?",
                "Why does every cell in our body contain DNA?",
                "Who was the first president of the United States?",
                "What is the difference between a list and a tuple in Python?");

        for (String question : questions) {
            System.out.println("\nQ: " + question);
            String answer = route(question);
            System.out.println("A: " + answer);
        }
    }
}
