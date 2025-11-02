package com.appointment.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequestDTO {
    private String token;
    private String title;
    private String message;
}
