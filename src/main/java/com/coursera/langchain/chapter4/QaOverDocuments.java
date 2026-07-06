package com.coursera.langchain.chapter4;

import com.coursera.langchain.HfConfig;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Chapter 4 — Q&amp;A over Documents: a Retrieval-Augmented Generation (RAG)
 * pipeline. Loads a product catalog CSV, embeds each row locally with an
 * in-process ONNX model (no API call, matching Python's local
 * sentence-transformers/all-MiniLM-L6-v2), indexes the embeddings in an
 * in-memory vector store, retrieves the rows most relevant to a question,
 * and "stuffs" them into a prompt for the chat model to answer from.
 *
 * <p>Java counterpart of Python's {@code qa_over_documents.py}. LangChain4j
 * has no {@code VectorstoreIndexCreator}/{@code index.query(...)} convenience
 * wrapper, so the loader → embed → store → retrieve → "stuff" prompt steps
 * are wired explicitly.
 */
public class QaOverDocuments {

    public static final String CSV_RESOURCE = "/data/outdoor_clothing.csv";
    private static final int TOP_K = 4;

    public static void main(String[] args) throws IOException {
        ChatModel chatModel = HfConfig.chatModel();
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        List<TextSegment> segments = loadCsvAsSegments(CSV_RESOURCE);
        InMemoryEmbeddingStore<TextSegment> store = buildStore(segments, embeddingModel);

        String query = "Please list all your shirts with sun protection in a table in markdown and summarize each one.";
        String response = answer(query, embeddingModel, store, chatModel);
        System.out.println(response);
    }

    /** Embeds every segment and indexes it in a fresh in-memory vector store. */
    public static InMemoryEmbeddingStore<TextSegment> buildStore(List<TextSegment> segments, EmbeddingModel embeddingModel) {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment).content();
            store.add(embedding, segment);
        }
        return store;
    }

    /** Embeds the question, retrieves the top matching rows, and "stuffs" them into a prompt (chain_type="stuff"). */
    public static String answer(String query, EmbeddingModel embeddingModel,
                                 InMemoryEmbeddingStore<TextSegment> store, ChatModel chatModel) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(TOP_K)
                .build();
        EmbeddingSearchResult<TextSegment> result = store.search(request);

        String context = result.matches().stream()
                .map(EmbeddingMatch::embedded)
                .map(TextSegment::text)
                .collect(Collectors.joining("\n\n"));

        PromptTemplate promptTemplate = PromptTemplate.from(
                "Use the following product catalog entries to answer the question. "
                        + "If the question asks for a list, format it as a markdown table.\n\n"
                        + "{{context}}\n\nQuestion: {{question}}");
        String prompt = promptTemplate.apply(Map.of("context", context, "question", query)).text();
        return chatModel.chat(prompt);
    }

    /** Minimal CSV loader: one {@link TextSegment} per row, formatted as "column: value" lines. */
    public static List<TextSegment> loadCsvAsSegments(String resourcePath) throws IOException {
        String csv;
        try (InputStream in = QaOverDocuments.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("CSV resource not found: " + resourcePath);
            }
            csv = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String[]> rows = parseCsv(csv);
        String[] header = rows.get(0);

        List<TextSegment> segments = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            StringBuilder content = new StringBuilder();
            for (int col = 0; col < header.length && col < row.length; col++) {
                if (col > 0) {
                    content.append('\n');
                }
                content.append(header[col]).append(": ").append(row[col]);
            }
            Metadata metadata = new Metadata()
                    .put("row", i - 1)
                    .put("source", resourcePath);
            segments.add(TextSegment.from(content.toString(), metadata));
        }
        return segments;
    }

    /** Small state-machine CSV parser supporting quoted fields with embedded commas/newlines. */
    private static List<String[]> parseCsv(String content) {
        List<String[]> rows = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                current.add(field.toString());
                field.setLength(0);
            } else if (c == '\r') {
                // ignored; paired '\n' below terminates the row
            } else if (c == '\n') {
                current.add(field.toString());
                field.setLength(0);
                rows.add(current.toArray(new String[0]));
                current = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (field.length() > 0 || !current.isEmpty()) {
            current.add(field.toString());
            rows.add(current.toArray(new String[0]));
        }
        return rows;
    }
}
