package xyz.kbws.app.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import xyz.kbws.api.IAiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kbws
 * @date 2025/4/13
 * @description:
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/ollama")
public class OllamaController implements IAiService {

    @Resource
    private OllamaChatClient chatClient;

    @Resource
    private PgVectorStore pgVectorStore;

    @GetMapping("/generate")
    @Override
    public ChatResponse generate(@RequestParam String model, @RequestParam String message) {
        return chatClient.call(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    @GetMapping("/generateStream")
    @Override
    public Flux<ChatResponse> generateStream(String model, String message) {
        return chatClient.stream(new Prompt(message, OllamaOptions.create().withModel(model)));
    }

    @GetMapping("/generateStreamRag")
    @Override
    public Flux<ChatResponse> generateStreamRag(@RequestParam("model") String model, @RequestParam("ragTag") String ragTag, @RequestParam("message") String message) {
        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        // 指定文档搜索
        SearchRequest request = SearchRequest.query(message)
                .withTopK(5)
                .withFilterExpression("knowledge == '" + ragTag + "'");

        List<Document> documents = pgVectorStore.similaritySearch(request);
        String documentCollectors = documents.stream().map(Document::getContent).collect(Collectors.joining());
        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

        List<Message> messages = new ArrayList<>();
        messages.add(ragMessage);
        messages.add(new UserMessage(message));

        return chatClient.stream(new Prompt(
                messages,
                OllamaOptions.create()
                        .withModel(model)
        ));
    }
}
