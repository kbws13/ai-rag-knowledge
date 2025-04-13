package xyz.kbws.api;

import org.springframework.ai.chat.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author kbws
 * @date 2025/4/13
 * @description:
 */
public interface IAiService {

    ChatResponse generate(String model, String message);

    Flux<ChatResponse> generateStream(String model, String message);

    Flux<ChatResponse> generateStreamRag(String model, String ragTag, String message);
}
