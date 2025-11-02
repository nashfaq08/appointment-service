package com.appointment.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class StripeDTO {
    private Long amount_subtotal;
    private Long amount_total;
    private String currency;
    private String payment_status;
    private String status;
    private Long created;
    private String client_reference_id;
    private Long payment_transaction_id;
    private List<String> payment_method_types;
    private boolean livemode;
    private String mode;

    // Appointment related
    private String appointmentDate;
    private String appointmentTypeId;
    private String description;
    private String endTime;
    private String startTime;
    private UUID lawyerId;

    private CustomerDetails customer_details;

    @Data
    public static class CustomerDetails {
        private String email;
        private String name;
        private Address address;

        @Data
        public static class Address {
            private String city;
            private String country;
            private String line1;
            private String line2;
            private String postal_code;
            private String state;
        }
    }
}
