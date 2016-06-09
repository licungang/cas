package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link PasswordEncoderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.authn.password", ignoreUnknownFields = false)
public class PasswordEncoderProperties {
    private String encodingAlgorithm;
    private String characterEncoding;

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getEncodingAlgorithm() {
        return encodingAlgorithm;
    }

    public void setEncodingAlgorithm(final String encodingAlgorithm) {
        this.encodingAlgorithm = encodingAlgorithm;
    }
}


