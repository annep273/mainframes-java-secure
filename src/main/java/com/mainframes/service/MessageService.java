package com.mainframes.service;

import com.mainframes.model.Message;
import com.mainframes.model.MessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for message operations
 */
@Service
@Slf4j
public class MessageService {

    private final ConcurrentHashMap<Long, Message> messages = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Message> getAllMessages() {
        log.info("Fetching all messages");
        return new ArrayList<>(messages.values());
    }

    public Optional<Message> getMessageById(Long id) {
        log.info("Fetching message with id: {}", id);
        return Optional.ofNullable(messages.get(id));
    }

    public Message createMessage(MessageRequest request) {
        Long id = idCounter.getAndIncrement();
        Message message = Message.builder()
                .id(id)
                .content(request.getContent())
                .author(request.getAuthor())
                .createdAt(LocalDateTime.now())
                .build();
        
        messages.put(id, message);
        log.info("Created message with id: {}", id);
        return message;
    }

    public void deleteMessage(Long id) {
        messages.remove(id);
        log.info("Deleted message with id: {}", id);
    }
}
