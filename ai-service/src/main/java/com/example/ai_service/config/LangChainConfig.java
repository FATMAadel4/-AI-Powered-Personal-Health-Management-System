package com.example.ai_service.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChainConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${chroma.url}")
    private String chromaUrl;

    @Value("${chroma.collection}")
    private String chromaCollection;

    @Bean
    public ChatLanguageModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .temperature(0.7)
                .maxTokens(1000)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(chromaUrl)
                .collectionName(chromaCollection)
                .timeout(Duration.ofSeconds(30))
                .build();
    }
}