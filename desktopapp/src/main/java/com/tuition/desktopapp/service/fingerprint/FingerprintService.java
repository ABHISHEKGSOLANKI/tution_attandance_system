package com.tuition.desktopapp.service.fingerprint;

public interface FingerprintService {

    FingerprintCaptureResult captureFingerprint();

    FingerprintMatchResult matchFingerprint(String capturedTemplate, String storedTemplate);

    default boolean supportsLocalMatching() {
        return true;
    }

    default String providerName() {
        return "Generic fingerprint provider";
    }
}
