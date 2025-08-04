package com.appointment.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AvailableLawyersResponse {

    private List<UUID> lawyerIds;

}
