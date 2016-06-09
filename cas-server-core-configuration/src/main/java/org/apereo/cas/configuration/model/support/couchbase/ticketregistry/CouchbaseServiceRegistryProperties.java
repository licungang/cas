package org.apereo.cas.configuration.model.support.couchbase.ticketregistry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link CouchbaseServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "svcreg.couchbase", ignoreUnknownFields = false)
public class CouchbaseServiceRegistryProperties {
    private boolean queryEnabled = true;

    private String nodeSet = "localhost:8091";

    private int timeout = 10;

    private String password;
    private String bucket = "default";

    public boolean isQueryEnabled() {
        return queryEnabled;
    }

    public void setQueryEnabled(final boolean queryEnabled) {
        this.queryEnabled = queryEnabled;
    }

    public String getNodeSet() {
        return nodeSet;
    }

    public void setNodeSet(final String nodeSet) {
        this.nodeSet = nodeSet;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }
}
