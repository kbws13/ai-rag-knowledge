package xyz.kbws.app.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import xyz.kbws.api.IAiService;

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

    @Override
    public Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message) {
        return null;
    }
}
