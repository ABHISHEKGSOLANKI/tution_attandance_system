package com.tuition.desktopapp.service.fingerprint;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.tuition.desktopapp.config.AppProperties;
import com.tuition.desktopapp.exception.DeviceUnavailableException;
import com.tuition.desktopapp.exception.InvalidFingerprintException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "middleware.fingerprint", name = "sdk-enabled", havingValue = "true")
public class SdkFingerprintService implements FingerprintService {

    private static final Logger log = LoggerFactory.getLogger(SdkFingerprintService.class);
    private final AppProperties properties;
    private NativeFingerprintLibrary library;

    public SdkFingerprintService(AppProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        try {
            String path = properties.getFingerprint().getSdkLibraryPath();
            library = Native.load(path, NativeFingerprintLibrary.class);
            log.info("Fingerprint SDK library loaded from {}", path);
        } catch (Exception ex) {
            throw new DeviceUnavailableException("Failed to load fingerprint SDK library", ex);
        }
    }

    @Override
    public FingerprintCaptureResult captureFingerprint() {
        throw new DeviceUnavailableException("Real SDK capture is a stub. Plug vendor methods into SdkFingerprintService.");
    }

    @Override
    public FingerprintMatchResult matchFingerprint(String capturedTemplate, String storedTemplate) {
        if (capturedTemplate == null || storedTemplate == null) {
            throw new InvalidFingerprintException("Fingerprint template is missing");
        }
        throw new DeviceUnavailableException("Real SDK matching is a stub. Plug vendor methods into SdkFingerprintService.");
    }

    interface NativeFingerprintLibrary extends Library {
        // Add vendor SDK signatures here, e.g. int Capture(...), int MatchTemplate(...).
    }
}
