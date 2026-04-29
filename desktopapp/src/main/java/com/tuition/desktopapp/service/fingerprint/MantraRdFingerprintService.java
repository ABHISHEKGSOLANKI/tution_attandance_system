package com.tuition.desktopapp.service.fingerprint;

import com.tuition.desktopapp.config.AppProperties;
import com.tuition.desktopapp.exception.DeviceUnavailableException;
import com.tuition.desktopapp.exception.InvalidFingerprintException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

@Service
@ConditionalOnProperty(prefix = "middleware.fingerprint", name = "rd-enabled", havingValue = "true")
public class MantraRdFingerprintService implements FingerprintService {

    private static final Logger log = LoggerFactory.getLogger(MantraRdFingerprintService.class);
    private final AppProperties properties;
    private final RestTemplate restTemplate;

    public MantraRdFingerprintService(AppProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public FingerprintCaptureResult captureFingerprint() {
        String captureUrl = properties.getFingerprint().getRdCaptureUrl();
        String requestXml = properties.getFingerprint().getRdCaptureRequestXml();
        if (captureUrl == null || captureUrl.isBlank()) {
            throw new DeviceUnavailableException("Mantra RD capture URL is not configured");
        }
        if (requestXml == null || requestXml.isBlank()) {
            throw new InvalidFingerprintException("Mantra RD capture request XML is not configured");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.ALL));

            ResponseEntity<String> response = restTemplate.exchange(
                    captureUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestXml, headers),
                    String.class
            );

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new DeviceUnavailableException("Mantra RD service returned an empty capture response");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(body)));

            String errCode = document.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errCode").getNodeValue();
            String errInfo = document.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("errInfo").getNodeValue();
            String quality = document.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("qScore") != null
                    ? document.getElementsByTagName("Resp").item(0).getAttributes().getNamedItem("qScore").getNodeValue()
                    : "UNKNOWN";

            if (!"0".equals(errCode)) {
                throw new InvalidFingerprintException("Mantra RD capture failed: " + errInfo + " (errCode=" + errCode + ")");
            }

            String data = document.getElementsByTagName("Data").item(0).getTextContent();
            if (data == null || data.isBlank()) {
                throw new InvalidFingerprintException("Mantra RD response did not contain biometric data");
            }

            log.info("Mantra RD fingerprint captured successfully with quality {}", quality);
            return new FingerprintCaptureResult(data, quality, false);
        } catch (InvalidFingerprintException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new DeviceUnavailableException("Unable to reach Mantra RD service at " + captureUrl, ex);
        } catch (Exception ex) {
            throw new DeviceUnavailableException("Failed to parse Mantra RD capture response", ex);
        }
    }

    @Override
    public FingerprintMatchResult matchFingerprint(String capturedTemplate, String storedTemplate) {
        throw new InvalidFingerprintException(
                "Mantra RD service does not expose reusable local templates for offline matching. " +
                        "Use the vendor SDK/DLL integration for true local biometric attendance."
        );
    }

    @Override
    public boolean supportsLocalMatching() {
        return false;
    }

    @Override
    public String providerName() {
        return "Mantra RD Service";
    }
}
