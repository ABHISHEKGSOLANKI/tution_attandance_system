package com.tuition.desktopapp.service.fingerprint;

public interface FingerprintService {

    FingerprintCaptureResult captureFingerprint();

    FingerprintMatchResult matchFingerprint(String capturedTemplate, String storedTemplate);
}
