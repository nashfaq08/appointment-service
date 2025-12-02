package com.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ServiceGroupDTO {
    private String category;
    private List<String> services;
}
