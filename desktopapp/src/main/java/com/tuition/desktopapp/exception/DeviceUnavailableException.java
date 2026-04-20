package com.tuition.desktopapp.exception;

public class DeviceUnavailableException extends RuntimeException {

    public DeviceUnavailableException(String message) {
        super(message);
    }

    public DeviceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
