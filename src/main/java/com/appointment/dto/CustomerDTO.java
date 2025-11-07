package com.appointment.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CustomerDTO {
    private UUID id;
    private String name;
    private String contactNo;
    private String email;
    private String nationalId;
}

