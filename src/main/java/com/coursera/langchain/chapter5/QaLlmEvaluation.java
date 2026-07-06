package com.coursera.langchain.chapter5;

import com.coursera.langchain.HfConfig;
import com.coursera.langchain.chapter4.QaOverDocuments;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chapter 5 — LLM-assisted evaluation: generates {@code (query, answer)}
 * ground-truth pairs directly from the product catalog with the LLM itself
 * (the LangChain4j equivalent of Python's {@code QAGenerateChain}), runs each
 * generated question through the chapter 4 RAG pipeline, then has the LLM
 * grade each prediction against the expected answer (the equivalent of
 * {@code QAEvalChain}) — an LLM-as-judge instead of keyword matching.
 *
 * <p>Java counterpart of Python's {@code qa_llm_evaluation.py}. LangChain4j
 * has no built-in {@code QAGenerateChain}/{@code QAEvalChain}, so both steps
 * are implemented as plain prompt templates over JSON output.
 */
public class QaLlmEvaluation {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int SAMPLE_SIZE = 5;

    private static final PromptTemplate GENERATE_TEMPLATE = PromptTemplate.from("""
            You are a teacher creating a quiz. Given the following document, \
            write one question that can be answered using only the information \
            in the document, along with the correct answer.

            Format the output as JSON with the following keys:
            query
            answer

            Respond with JSON only, no markdown fences.
            Document: {{doc}}""");

    private static final PromptTemplate GRADE_TEMPLATE = PromptTemplate.from("""
            You are grading the following question based on a reference answer \
            and a student's answer. Respond with CORRECT if the student's answer \
            is factually consistent with the reference answer, otherwise respond \
            with INCORRECT. Respond with a single word only.

            Question: {{query}}
            Reference Answer: {{answer}}
            Student Answer: {{predicted}}
            Grade:""");

    /** A generated ground-truth example: a question and its LLM-generated expected answer. */
    record Example(String query, String answer) {
    }

    public static void main(String[] args) throws IOException {
        ChatModel chatModel = HfConfig.chatModel();
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        List<TextSegment> segments = QaOverDocuments.loadCsvAsSegments(QaOverDocuments.CSV_RESOURCE);
        InMemoryEmbeddingStore<TextSegment> store = QaOverDocuments.buildStore(segments, embeddingModel);

        // Generate one QA pair per document (use a subset to keep runtime reasonable)
        List<TextSegment> sample = segments.subList(0, Math.min(SAMPLE_SIZE, segments.size()));
        List<Example> examples = new ArrayList<>();
        for (TextSegment segment : sample) {
            examples.add(generateExample(segment, chatModel));
        }

        // Run predictions through the RAG pipeline
        List<String> predictions = new ArrayList<>();
        for (Example example : examples) {
            predictions.add(QaOverDocuments.answer(example.query(), embeddingModel, store, chatModel));
        }

        System.out.println("=".repeat(70));
        System.out.println("LLM-ASSISTED EVALUATION");
        System.out.println("=".repeat(70));

        int correct = 0;
        for (int i = 0; i < examples.size(); i++) {
            Example example = examples.get(i);
            String predicted = predictions.get(i);
            String grade = grade(example, predicted, chatModel);
            boolean isCorrect = grade.contains("CORRECT") && !grade.contains("INCORRECT");
            if (isCorrect) {
                correct++;
            }

            System.out.println();
            System.out.println("Example " + (i + 1));
            System.out.println("  Query    : " + example.query());
            System.out.println("  Answer   : " + example.answer());
            System.out.println("  Predicted: " + predicted.trim());
            System.out.println("  LLM Grade: " + grade);
        }

        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("Score: " + correct + "/" + examples.size()
                + " (" + (100 * correct / examples.size()) + "%)");
        System.out.println("=".repeat(70));
    }

    /** Asks the LLM to write a question/answer pair grounded in a single document (QAGenerateChain equivalent). */
    private static Example generateExample(TextSegment segment, ChatModel chatModel) throws IOException {
        String prompt = GENERATE_TEMPLATE.apply(Map.of("doc", segment.text())).text();
        String raw = chatModel.chat(prompt);
        JsonNode json = MAPPER.readTree(stripFences(raw));
        return new Example(json.path("query").asText(), json.path("answer").asText());
    }

    /** Asks the LLM to grade a prediction against the reference answer (QAEvalChain equivalent). */
    private static String grade(Example example, String predicted, ChatModel chatModel) {
        String prompt = GRADE_TEMPLATE.apply(Map.of(
                "query", example.query(),
                "answer", example.answer(),
                "predicted", predicted.trim())).text();
        return chatModel.chat(prompt).trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static String stripFences(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```(json)?", "").replaceFirst("```$", "").trim();
        }
        return t;
    }
}
