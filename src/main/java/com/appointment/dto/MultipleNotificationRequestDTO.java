package com.appointment.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class MultipleNotificationRequestDTO {
    private List<String> tokens;
    private String title;
    private String message;
}
