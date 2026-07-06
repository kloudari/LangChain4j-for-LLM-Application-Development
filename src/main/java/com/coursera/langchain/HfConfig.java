package com.coursera.langchain;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Duration;

/**
 * Shared Hugging Face configuration.
 *
 * <p>Reads {@code HF_TOKEN} and {@code HF_MODEL} from a local {@code .env} file
 * (or process environment) and builds a ready-to-use chat model that targets
 * Hugging Face's OpenAI-compatible router ({@code router.huggingface.co}).
 */
public final class HfConfig {

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    /** Hugging Face router exposes an OpenAI-compatible Chat Completions API. */
    private static final String HF_BASE_URL = "https://router.huggingface.co/v1";

    public static final String HF_TOKEN = resolveToken();
    public static final String HF_MODEL = get("HF_MODEL", "Qwen/Qwen2.5-7B-Instruct");

    private HfConfig() {
    }

    private static String get(String key, String defaultValue) {
        String value = DOTENV.get(key);
        return value != null ? value : defaultValue;
    }

    private static String resolveToken() {
        String token = DOTENV.get("HF_TOKEN");
        if (token == null) {
            token = DOTENV.get("HUGGINGFACEHUB_API_TOKEN");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "Missing Hugging Face token. Set HF_TOKEN or HUGGINGFACEHUB_API_TOKEN in your .env file.");
        }
        return token;
    }

    /** Builds a chat model for the default {@code HF_MODEL}. */
    public static ChatModel chatModel() {
        return chatModel(HF_MODEL);
    }

    /** Builds a chat model for a specific model id. */
    public static ChatModel chatModel(String modelId) {
        return OpenAiChatModel.builder()
                .baseUrl(HF_BASE_URL)
                .apiKey(HF_TOKEN)
                .modelName(modelId)
                .temperature(0.0)
                .maxTokens(512)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}
