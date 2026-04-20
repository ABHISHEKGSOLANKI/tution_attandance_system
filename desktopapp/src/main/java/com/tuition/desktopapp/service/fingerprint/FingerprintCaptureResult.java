package com.tuition.desktopapp.service.fingerprint;

public record FingerprintCaptureResult(String template, String quality, boolean mock) {
}
