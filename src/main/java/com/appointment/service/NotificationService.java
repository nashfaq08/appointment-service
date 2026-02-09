package com.appointment.service;

import com.appointment.entities.Appointment;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

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

    public void sendNotificationToCustomer(String token, String title, String message, Appointment appointment) {
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
                    .putData("appointmentId", appointment.getId().toString())
                    .putData("appointmentType", appointment.getAppointmentType().getName())
                    .putData("appointmentDate", appointment.getAppointmentDate().toString())
                    .putData("lawyerId", appointment.getLawyerId().toString())
                    .putData("startTime", appointment.getStartTime().toString())
                    .putData("endTime", appointment.getEndTime().toString())
                    .build();

            String response = FirebaseMessaging.getInstance().send(msg);
            log.info("Notification sent successfully {}", response);
        } catch (Exception e) {
            log.error("Failed to send notification to customer {}: {}", token, e.getMessage());
        }
    }

    @Async
    public void sendToMultipleDevices(List<String> tokens,
                                      String title,
                                      String body,
                                      UUID customerId,
                                      Appointment appointment) {
        log.info("Sending notification to multiple devices");

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("eventType", "OPEN_APPOINTMENT")
                .putData("appointmentId", appointment.getId().toString())
                .putData("appointmentType", appointment.getAppointmentType().getName())
                .putData("appointmentDate", appointment.getAppointmentDate().toString())
                .putData("customerId", customerId.toString())
                .putData("startTime", appointment.getStartTime().toString())
                .putData("endTime", appointment.getEndTime().toString())
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
