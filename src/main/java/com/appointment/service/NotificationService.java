package com.appointment.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    public void sendNotificationToDevice(String token, String title, String message) {
        try {
            log.info("Sending notification to device with token {}", token);
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(message)
                    .build();

            log.info("Building message to contain the notification");
            Message msg = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(msg);
            log.info("Notification sent successfully {}", response);
        } catch (Exception e) {
            log.error("Failed to send notification to token {}: {}", token, e.getMessage());
        }
    }

    @Async
    public void sendToMultipleDevices(List<String> tokens, String title, String body) {
        log.info("Sending notification to multiple devices");

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            BatchResponse response =
                    FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info(
                    "Notification sent. success={}, failure={}",
                    response.getSuccessCount(),
                    response.getFailureCount()
            );

        } catch (FirebaseMessagingException e) {
            log.error("FCM error", e);
        }
    }
}
