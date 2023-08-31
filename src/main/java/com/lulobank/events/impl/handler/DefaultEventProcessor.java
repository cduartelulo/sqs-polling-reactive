package com.lulobank.events.impl.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lulobank.events.api.handler.Event;
import com.lulobank.events.api.handler.EventHandler;
import com.lulobank.events.api.handler.EventProcessor;
import com.lulobank.events.api.handler.EventRegistry;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Map;

import static com.lulobank.events.api.utils.EventUtils.json;

/**
 * Default implementation for Event Processor
 * @author Carlos Duarte
 * See {@link EventProcessor} for more information
 */
@Slf4j
public class DefaultEventProcessor implements EventProcessor {

    private final EventRegistry eventRegistry;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DefaultEventProcessor(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
        objectMapper.registerModule(new JavaTimeModule());
    }

    public Either<?, Void> handle(String event) {
        return this.handle(event, null);

    }

    public Either<?, Void> handle(String event, Map<String, String> attributes) {
        Assert.isTrue(event != null && !event.isEmpty(), "Event cannot be null or empty");
        return Option.of(eventRegistry.handlers().get(getEventType(event)))
                .map(eh -> process(eh, event))
                .onEmpty(() -> log.error("Unknown Event Handler {}", json(event)))
                .getOrElse(Either.left(null));
    }

    public Try<Void> handleTry(String event, Map<String, String> attributes) {
        return this.handle(event, attributes)
                .fold(exception -> Try.failure(new RuntimeException("Unknown Event Handler " + json(event))),
                        Try::success);
    }

    private String getEventType(String event) {
        return Try.of(() -> objectMapper.readValue(event, JsonNode.class))
                .mapTry(e -> e.get("eventType").asText())
                .onFailure(ex -> log.error("Error getting eventType", ex))
                .getOrElse("");
    }

    private <T> Either<Event<T>, Void> process(EventHandler<T> eh, String event) {
        Try<Event<T>> events = readValue(eh, event);
        return events
                .flatMap(e -> eh.execute(e.getPayload()))
                .toEither(events.get());
    }

    private <T> Try<Event<T>> readValue(EventHandler<T> eh, String event) {
        return Try.of(() -> objectMapper.<Event<T>>readValue(event, objectMapper.getTypeFactory()
                        .constructParametricType(Event.class, eh.eventClass())))
                .flatMap(e -> Try.of(() -> eh.customListener(e)))
                .onFailure(ex -> log.error("Error reading object " + event, ex));
    }

}
