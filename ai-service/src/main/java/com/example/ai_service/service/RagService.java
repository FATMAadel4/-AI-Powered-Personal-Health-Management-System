package com.example.ai_service.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private ChatLanguageModel chatModel;

    public String indexPdf(MultipartFile file) throws IOException {

        // 1. اقرأ الـ PDF
        Document document = new ApachePdfBoxDocumentParser()
                .parse(file.getInputStream());

        // 2. تأكد إن فيه text
        if (document.text() == null || document.text().isBlank()) {
            return "PDF is empty or contains no readable text. " +
                    "Please upload a text-based PDF, not a scanned image.";
        }

        // 3. قطّعه لأجزاء صغيرة
        List<TextSegment> segments = DocumentSplitters
                .recursive(500, 50)
                .split(document);

        // 4. حوّل كل جزء لـ vector
        List<Embedding> embeddings = embeddingModel
                .embedAll(segments)
                .content();

        // 5. احفظهم في ChromaDB
        embeddingStore.addAll(embeddings, segments);

        return "PDF indexed successfully — " + segments.size() + " segments stored";
    }

    public String askAboutReports(String question) {

        // 1. حوّل السؤال لـ vector
        Embedding questionEmbedding = embeddingModel
                .embed(question)
                .content();

        // 2. جيب أقرب 5 أجزاء من ChromaDB
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.findRelevant(questionEmbedding, 5);

        // 3. لو مفيش نتائج
        if (matches.isEmpty()) {
            return "No relevant information found in your reports. Please upload your medical reports first.";
        }

        // 4. جمّع الأجزاء كـ context
        String context = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        // 5. اسأل GPT مع الـ context
        PromptTemplate template = PromptTemplate.from(
                "You are a helpful medical assistant. " +
                        "Based on the following patient medical reports:\n\n" +
                        "{{context}}\n\n" +
                        "Answer this question: {{question}}\n\n" +
                        "If the information is not in the reports, say so clearly. " +
                        "Always recommend consulting a doctor for medical decisions."
        );

        Prompt prompt = template.apply(Map.of(
                "context", context,
                "question", question
        ));

        return chatModel.generate(prompt.text());
    }
}