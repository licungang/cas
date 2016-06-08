package org.apereo.cas.configuration.model.webapp;

import org.apereo.cas.configuration.model.core.util.AbstractCryptographyProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Configuration properties class for webflow.*
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "webflow", ignoreUnknownFields = false)
public class WebflowProperties extends AbstractCryptographyProperties {

    private boolean refresh = true;

    private boolean alwaysPauseRedirect = false;

    private boolean redirectSameState = false;

    private Session session = new Session();

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(final boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isAlwaysPauseRedirect() {
        return alwaysPauseRedirect;
    }

    public void setAlwaysPauseRedirect(final boolean alwaysPauseRedirect) {
        this.alwaysPauseRedirect = alwaysPauseRedirect;
    }

    public boolean isRedirectSameState() {
        return redirectSameState;
    }

    public void setRedirectSameState(final boolean redirectSameState) {
        this.redirectSameState = redirectSameState;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(final Session session) {
        this.session = session;
    }

    public static class Session {
        private int lockTimeout = 30;
        private int maxConversations = 5;
        private boolean compress = false;
        private boolean storage = true;
        private Resource hzLocation = new ClassPathResource("hazelcast.xml");

        public int getLockTimeout() {
            return lockTimeout;
        }

        public void setLockTimeout(final int lockTimeout) {
            this.lockTimeout = lockTimeout;
        }

        public int getMaxConversations() {
            return maxConversations;
        }

        public void setMaxConversations(final int maxConversations) {
            this.maxConversations = maxConversations;
        }

        public boolean isCompress() {
            return compress;
        }

        public void setCompress(final boolean compress) {
            this.compress = compress;
        }

        public boolean isStorage() {
            return storage;
        }

        public void setStorage(final boolean storage) {
            this.storage = storage;
        }

        public Resource getHzLocation() {
            return hzLocation;
        }

        public void setHzLocation(final Resource hzLocation) {
            this.hzLocation = hzLocation;
        }
    }
}
