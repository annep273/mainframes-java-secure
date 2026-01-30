package com.mainframes.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message Request model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    
    @NotBlank(message = "Content cannot be blank")
    @Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
    private String content;
    
    @NotBlank(message = "Author cannot be blank")
    @Size(min = 1, max = 100, message = "Author name must be between 1 and 100 characters")
    private String author;
}
