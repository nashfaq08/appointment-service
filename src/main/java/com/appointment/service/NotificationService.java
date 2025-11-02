package com.appointment.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class NotificationService {

    public void sendNotificationToDevice(String token, String title, String message) {
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
        try {
            String response = FirebaseMessaging.getInstance().send(msg);
            log.info("Notification sent successfully {}", response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

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
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("Notification sent to " + response.getSuccessCount() + " devices successfully.");
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
