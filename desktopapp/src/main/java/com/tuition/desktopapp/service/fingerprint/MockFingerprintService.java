package com.tuition.desktopapp.service.fingerprint;

import com.tuition.desktopapp.config.AppProperties;
import com.tuition.desktopapp.exception.DeviceUnavailableException;
import com.tuition.desktopapp.exception.InvalidFingerprintException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(FingerprintService.class)
public class MockFingerprintService implements FingerprintService {

    private static final Logger log = LoggerFactory.getLogger(MockFingerprintService.class);
    private final AppProperties appProperties;

    public MockFingerprintService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public FingerprintCaptureResult captureFingerprint() {
        try {
            // Deterministic mock template for local testing if needed.
            String templateSeed = "MOCK-FP-" + System.currentTimeMillis();
            String template = Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(templateSeed.getBytes(StandardCharsets.UTF_8))
            );
            log.info("Mock fingerprint captured successfully");
            return new FingerprintCaptureResult(template, "GOOD", true);
        } catch (Exception ex) {
            throw new DeviceUnavailableException("Mock fingerprint device failed to capture template", ex);
        }
    }

    @Override
    public FingerprintMatchResult matchFingerprint(String capturedTemplate, String storedTemplate) {
        if (capturedTemplate == null || capturedTemplate.isBlank() || storedTemplate == null || storedTemplate.isBlank()) {
            throw new InvalidFingerprintException("Fingerprint template is missing or invalid");
        }
        boolean matched = capturedTemplate.equals(storedTemplate);
        int score = matched ? 100 : Math.max(0, 100 - levenshteinDistance(capturedTemplate, storedTemplate));
        int threshold = appProperties.getFingerprint().getMatchingThreshold();
        boolean thresholdMatch = matched || score >= threshold;
        return new FingerprintMatchResult(thresholdMatch, score);
    }

    @Override
    public String providerName() {
        return "Mock fingerprint provider";
    }

    private int levenshteinDistance(String left, String right) {
        int[][] dp = new int[left.length() + 1][right.length() + 1];
        for (int i = 0; i <= left.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        return dp[left.length()][right.length()];
    }
}
