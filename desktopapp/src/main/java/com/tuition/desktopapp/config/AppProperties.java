package com.tuition.desktopapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "middleware")
public class AppProperties {

    private final Encryption encryption = new Encryption();
    private final Backend backend = new Backend();
    private final Fingerprint fingerprint = new Fingerprint();

    public Encryption getEncryption() { return encryption; }
    public Backend getBackend() { return backend; }
    public Fingerprint getFingerprint() { return fingerprint; }

    public static class Encryption {
        private String secretKey;
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }

    public static class Backend {
        private String baseUrl;
        private String attendanceBulkPath;
        private String apiKey;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getAttendanceBulkPath() { return attendanceBulkPath; }
        public void setAttendanceBulkPath(String attendanceBulkPath) { this.attendanceBulkPath = attendanceBulkPath; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    public static class Fingerprint {
        private boolean rdEnabled;
        private String rdCaptureUrl;
        private String rdCaptureRequestXml;
        private boolean sdkEnabled;
        private String sdkLibraryPath;
        private int matchingThreshold = 95;
        public boolean isRdEnabled() { return rdEnabled; }
        public void setRdEnabled(boolean rdEnabled) { this.rdEnabled = rdEnabled; }
        public String getRdCaptureUrl() { return rdCaptureUrl; }
        public void setRdCaptureUrl(String rdCaptureUrl) { this.rdCaptureUrl = rdCaptureUrl; }
        public String getRdCaptureRequestXml() { return rdCaptureRequestXml; }
        public void setRdCaptureRequestXml(String rdCaptureRequestXml) { this.rdCaptureRequestXml = rdCaptureRequestXml; }
        public boolean isSdkEnabled() { return sdkEnabled; }
        public void setSdkEnabled(boolean sdkEnabled) { this.sdkEnabled = sdkEnabled; }
        public String getSdkLibraryPath() { return sdkLibraryPath; }
        public void setSdkLibraryPath(String sdkLibraryPath) { this.sdkLibraryPath = sdkLibraryPath; }
        public int getMatchingThreshold() { return matchingThreshold; }
        public void setMatchingThreshold(int matchingThreshold) { this.matchingThreshold = matchingThreshold; }
    }
}
