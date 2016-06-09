package org.apereo.cas.configuration.model.core.ticket;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link ProxyGrantingTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "pgt", ignoreUnknownFields = false)
public class ProxyGrantingTicketProperties {
    private int maxLength = 50;

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }
}
