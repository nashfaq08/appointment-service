package com.appointment.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SingleNotificationRequestDTO {
    private UUID userId;
    private String title;
    private String message;
}
