package com.coursera.langchain.chapter6;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Wikipedia lookup tool, calling Wikipedia's public REST/MediaWiki APIs
 * directly over HTTP. Java counterpart of Python's
 * {@code WikipediaQueryRun}/{@code WikipediaAPIWrapper} (which wrap the
 * {@code wikipedia} PyPI package) — no extra dependency is needed since the
 * JDK's built-in {@link HttpClient} and the project's existing Jackson
 * dependency are enough.
 */
public class WikipediaTool {

    private static final int MAX_EXTRACT_CHARS = 2000;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Tool("""
            Search Wikipedia and return a short summary for the given query. \
            Use this to look up facts about people, places, books, events, \
            organizations, etc.""")
    public String wikipedia(@P("the search query, e.g. a person's name or topic") String query) {
        try {
            String title = findTopMatchingTitle(query);
            if (title == null) {
                return "No Wikipedia results found for: " + query;
            }
            String extract = fetchSummary(title);
            if (extract == null || extract.isBlank()) {
                return "No summary available for: " + title;
            }
            return "Page: " + title + "\n" + truncate(extract, MAX_EXTRACT_CHARS);
        } catch (Exception e) {
            return "Error querying Wikipedia: " + e.getMessage();
        }
    }

    private String findTopMatchingTitle(String query) throws Exception {
        String url = "https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&srlimit=1&srsearch="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        JsonNode root = get(url);
        JsonNode hits = root.path("query").path("search");
        if (!hits.isArray() || hits.isEmpty()) {
            return null;
        }
        return hits.get(0).path("title").asText();
    }

    private String fetchSummary(String title) throws Exception {
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/"
                + URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20");
        JsonNode root = get(url);
        return root.path("extract").asText(null);
    }

    private JsonNode get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "LangChain4j-course-bot/1.0 (educational use)")
                .GET()
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return MAPPER.readTree(response.body());
    }

    private static String truncate(String text, int maxChars) {
        return text.length() <= maxChars ? text : text.substring(0, maxChars) + "...";
    }
}
