package com.appointment.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class BulkNotificationRequestDTO {
    private List<UUID> userIds;
    private String title;
    private String message;
}
