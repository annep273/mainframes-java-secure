package com.mainframes.controller;

import com.mainframes.model.Message;
import com.mainframes.model.MessageRequest;
import com.mainframes.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Message Controller for managing messages
 */
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Message management endpoints")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "Get all messages", description = "Retrieves all messages")
    public ResponseEntity<List<Message>> getAllMessages() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID", description = "Retrieves a specific message by its ID")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new message", description = "Creates a new message")
    public ResponseEntity<Message> createMessage(@Valid @RequestBody MessageRequest request) {
        Message message = messageService.createMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message", description = "Deletes a message by ID")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
