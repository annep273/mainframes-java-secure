package com.mainframes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainframes.model.Message;
import com.mainframes.model.MessageRequest;
import com.mainframes.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @Test
    void testGetAllMessages() throws Exception {
        Message message1 = Message.builder()
                .id(1L)
                .content("Test message 1")
                .author("Author 1")
                .createdAt(LocalDateTime.now())
                .build();

        Message message2 = Message.builder()
                .id(2L)
                .content("Test message 2")
                .author("Author 2")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.getAllMessages()).thenReturn(Arrays.asList(message1, message2));

        mockMvc.perform(get("/api/v1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test message 1"))
                .andExpect(jsonPath("$[1].content").value("Test message 2"));
    }

    @Test
    void testGetMessageById() throws Exception {
        Message message = Message.builder()
                .id(1L)
                .content("Test message")
                .author("Test Author")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.getMessageById(1L)).thenReturn(Optional.of(message));

        mockMvc.perform(get("/api/v1/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    void testCreateMessage() throws Exception {
        MessageRequest request = MessageRequest.builder()
                .content("New message")
                .author("New Author")
                .build();

        Message createdMessage = Message.builder()
                .id(1L)
                .content("New message")
                .author("New Author")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(any(MessageRequest.class))).thenReturn(createdMessage);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("New message"));
    }

    @Test
    void testDeleteMessage() throws Exception {
        mockMvc.perform(delete("/api/v1/messages/1"))
                .andExpect(status().isNoContent());
    }
}
