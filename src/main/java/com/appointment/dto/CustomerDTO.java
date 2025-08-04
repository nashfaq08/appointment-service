package com.appointment.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long custId;
    private String name;
    private String contactNo;
    private String email;
    private String nationalId;
}

