package com.coursera.langchain.chapter5;

import com.coursera.langchain.HfConfig;
import com.coursera.langchain.chapter4.QaOverDocuments;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Chapter 5 — Manual evaluation: runs a hand-coded set of
 * {@code (query, expected answer)} pairs through the chapter 4 RAG pipeline
 * and scores each prediction with a simple keyword-overlap heuristic.
 *
 * <p>Java counterpart of Python's {@code qa_manual_evaluation.py}.
 */
public class QaManualEvaluation {

    /** One hard-coded ground-truth example: a question and its expected answer. */
    record Example(String query, String expected) {
    }

    private static final List<Example> EXAMPLES = Arrays.asList(
            new Example(
                    "Do the SunShield Hiking Shirt offer sun protection?",
                    "Yes, the SunShield Hiking Shirt offers UPF 50+ sun protection."),
            new Example(
                    "What is the fill power of the DownDrift Insulated Jacket?",
                    "The DownDrift Insulated Jacket has 700-fill-power down."),
            new Example(
                    "Which product is recommended for wet mountain conditions?",
                    "The ArcticShell Waterproof Jacket is ideal for wet mountain conditions."),
            new Example(
                    "Can the SummitTrek Hiking Pants be converted to shorts?",
                    "Yes, the SummitTrek Hiking Pants have zip-off lower legs that convert them to shorts."),
            new Example(
                    "What type of wool is used in the AlpineBase Merino Top?",
                    "The AlpineBase Merino Top uses 18.5-micron merino wool."));

    public static void main(String[] args) throws IOException {
        ChatModel chatModel = HfConfig.chatModel();
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        List<TextSegment> segments = QaOverDocuments.loadCsvAsSegments(QaOverDocuments.CSV_RESOURCE);
        InMemoryEmbeddingStore<TextSegment> store = QaOverDocuments.buildStore(segments, embeddingModel);

        System.out.println("=".repeat(70));
        System.out.println("MANUAL EVALUATION");
        System.out.println("=".repeat(70));

        int correct = 0;
        for (int i = 0; i < EXAMPLES.size(); i++) {
            Example example = EXAMPLES.get(i);
            String predicted = QaOverDocuments.answer(example.query(), embeddingModel, store, chatModel);
            boolean isCorrect = isKeywordMatch(example.expected(), predicted);
            if (isCorrect) {
                correct++;
            }

            System.out.println();
            System.out.println("Example " + (i + 1));
            System.out.println("  Query    : " + example.query());
            System.out.println("  Expected : " + example.expected());
            System.out.println("  Predicted: " + predicted.trim());
            System.out.println("  Result   : " + (isCorrect ? "CORRECT" : "INCORRECT"));
        }

        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("Score: " + correct + "/" + EXAMPLES.size()
                + " (" + (100 * correct / EXAMPLES.size()) + "%)");
        System.out.println("=".repeat(70));
    }

    /** Checks whether at least half of the expected answer's significant (5+ letter) words appear in the prediction. */
    private static boolean isKeywordMatch(String expected, String predicted) {
        String predictedLower = predicted.toLowerCase(Locale.ROOT);
        List<String> keyWords = Arrays.stream(expected.toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(w -> w.length() > 4)
                .toList();
        long matches = keyWords.stream().filter(predictedLower::contains).count();
        return matches >= Math.max(1, keyWords.size() / 2);
    }
}
