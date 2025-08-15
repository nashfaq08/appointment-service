//package com.appointment.service;
//
//import com.google.firebase.messaging.*;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//public class NotificationService {
//
//    public void sendNotificationToDevice(String token, String title, String message) {
//        Notification notification = Notification.builder()
//                .setTitle(title)
//                .setBody(message)
//                .build();
//
//        Message msg = Message.builder()
//                .setToken(token)
//                .setNotification(notification)
//                .build();
//
//        try {
//            String response = FirebaseMessaging.getInstance().send(msg);
//            System.out.println("Successfully sent message: " + response);
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void sendToMultipleDevices(List<String> tokens, String title, String body) {
//        MulticastMessage message = MulticastMessage.builder()
//                .addAllTokens(tokens)
//                .setNotification(Notification.builder()
//                        .setTitle(title)
//                        .setBody(body)
//                        .build())
//                .build();
//
//        try {
//            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
//            System.out.println("Sent to " + response.getSuccessCount() + " devices");
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
//    }
//}
