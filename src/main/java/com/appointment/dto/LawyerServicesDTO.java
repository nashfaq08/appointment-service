package com.appointment.dto;

import lombok.Data;

import java.util.List;

@Data
public class LawyerServicesDTO {
    private List<ServiceGroupDTO> services;
    private String lawyerEmail;
}

