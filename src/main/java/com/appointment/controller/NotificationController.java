package com.appointment.controller;

import com.appointment.dto.MultipleNotificationRequestDTO;
import com.appointment.dto.NotificationRequestDTO;
import com.appointment.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/send")
    public ResponseEntity<String> sendNotificationToDevice(@RequestBody NotificationRequestDTO request) {
        notificationService.sendNotificationToDevice(request.getToken(), request.getTitle(), request.getMessage());
        return ResponseEntity.ok("Notification sent to device");
    }

//    @PreAuthorize("hasRole('CUSTOMER')")
//    @PostMapping("/send-multiple")
//    public ResponseEntity<String> sendNotificationToMultipleDevices(@RequestBody MultipleNotificationRequestDTO request) {
//        notificationService.sendToMultipleDevices(request.getTokens(), request.getTitle(), request.getMessage());
//        return ResponseEntity.ok("Notifications sent to multiple devices");
//    }

}
