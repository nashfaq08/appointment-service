package com.appointment.service;


import com.appointment.config.EmailProperties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailProperties emailProps;

    public void sendAppointmentBookingEmail(String toEmail, String customerName, String appointmentType,
                                            String appointmentDate, String appointmentStartTime) {
        Session session = createSession();
        String emailContent = emailProps.getBody()
                .replace("%{appointment.customerName}", customerName)
                .replace("%{appointment.appointmentType}", appointmentType)
                .replace("%{appointment.appointmentDate}", appointmentDate)
                .replace("%{appointment.appointmentStartTime}", appointmentStartTime);
        sendEmail(session, toEmail, emailProps.getSubject(), emailContent);
    }

    private Session createSession() {
        log.info("Creating session for sending the email");
        Properties props = new Properties();
        props.put("mail.smtp.ssl.protocols", emailProps.getSmtp().getProtocols());
        props.put("mail.smtp.ssl.trust", emailProps.getSmtp().getTrust());
        props.put("mail.smtp.host", emailProps.getSmtp().getHost());
        props.put("mail.smtp.port", emailProps.getSmtp().getPort());
        props.put("mail.smtp.auth", String.valueOf(emailProps.getSmtp().isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(emailProps.getSmtp().isStarttls()));
        props.put("mail.transport.protocol", emailProps.getSmtp().getProtocol());

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        emailProps.getFrom().getAddress(),
                        emailProps.getFrom().getPassword()
                );
            }
        });
    }

    private void sendEmail(Session session, String toEmail, String subject, String body) {
        try {
            log.info("Sending the email for appointment booking");
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(
                    emailProps.getFrom().getAddress(),
                    emailProps.getFrom().getName()));

            msg.setReplyTo(InternetAddress.parse(emailProps.getFrom().getAddress(), false));
            msg.setSubject(subject, "UTF-8");

            msg.setContent(body, "text/html; charset=UTF-8");

            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            Transport.send(msg);
            log.info("Appointment booking email sent with HTML content!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
