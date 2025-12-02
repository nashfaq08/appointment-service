package com.appointment.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LawyerDetailsDTO {
    private List<ServiceGroupDTO> services;
    private UUID id;
}
