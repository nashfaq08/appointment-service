package com.appointment.exception;

import java.util.UUID;

public class NoDeviceTokenFoundException extends RuntimeException {
    private final UUID userAuthId;

    public NoDeviceTokenFoundException(UUID userAuthId) {
        super("No device token found for userAuthId = " + userAuthId);
        this.userAuthId = userAuthId;
    }
}
