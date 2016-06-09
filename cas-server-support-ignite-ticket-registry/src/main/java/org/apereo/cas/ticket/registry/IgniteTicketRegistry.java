package org.apereo.cas.ticket.registry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteState;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.ssl.SslContextFactory;
import org.apereo.cas.configuration.model.support.ignite.IgniteProperties;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * <p>
 * <a href="https://ignite.apache.org">Ignite</a> based distributed ticket registry.
 * </p>
 * <p>
 * <p>
 * Use distinct caches for ticket granting tickets (TGT) and service tickets (ST) for:
 * </p>
 * <ul>
 * <li>Tuning : use cache level time to live with different values for TGT an ST.</li>
 * <li>Monitoring : follow separately the number of TGT and ST.</li>
 * </ul>
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0`
 */
public class IgniteTicketRegistry extends AbstractTicketRegistry {

    @Autowired
    private IgniteProperties igniteProperties;

    @Resource(name = "igniteConfiguration")
    private IgniteConfiguration igniteConfiguration;

    private IgniteCache<String, Ticket> ticketIgniteCache;

    private Ignite ignite;

    /**
     * @see #setSupportRegistryState(boolean)
     **/
    private boolean supportRegistryState = true;

    /**
     * Instantiates a new Ignite ticket registry.
     */
    public IgniteTicketRegistry() {
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);
        logger.debug("Adding ticket {} to the cache {}", ticket.getId(), this.ticketIgniteCache.getName());
        this.ticketIgniteCache.withExpiryPolicy(new ExpiryPolicy() {
            @Override
            public Duration getExpiryForCreation() {
                return new Duration(TimeUnit.SECONDS, ticket.getExpirationPolicy().getTimeToLive());
            }

            @Override
            public Duration getExpiryForAccess() {
                return new Duration(TimeUnit.SECONDS, ticket.getExpirationPolicy().getTimeToIdle());
            }

            @Override
            public Duration getExpiryForUpdate() {
                return new Duration(TimeUnit.SECONDS, ticket.getExpirationPolicy().getTimeToLive());
            }
        }).put(ticket.getId(), ticket);
    }


    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final Ticket ticket = getTicket(ticketId);
        if (ticket != null) {
            return this.ticketIgniteCache.remove(ticket.getId());
        }
        return true;
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        final String ticketId = encodeTicketId(ticketIdToGet);
        if (ticketId == null) {
            return null;
        }

        final Ticket ticket = this.ticketIgniteCache.get(ticketId);
        if (ticket == null) {
            logger.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }
        return decodeTicket(ticket);
    }

    @Override
    public Collection<Ticket> getTickets() {
        final QueryCursor<Cache.Entry<String, Ticket>> cursor = this.ticketIgniteCache.query(new ScanQuery<>((key, t) -> !t.isExpired()));
        return decodeTickets(cursor.getAll().stream().map(Cache.Entry::getValue).collect(toList()));
    }

    public void setTicketIgniteCache(final IgniteCache<String, Ticket> ticketIgniteCache) {
        this.ticketIgniteCache = ticketIgniteCache;
    }

    public void setIgniteConfiguration(final IgniteConfiguration igniteConfiguration) {
        this.igniteConfiguration = igniteConfiguration;
    }

    public IgniteConfiguration getIgniteConfiguration() {
        return this.igniteConfiguration;
    }


    @Override
    public void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }

    /**
     * Flag to indicate whether this registry instance should participate in reporting its state with
     * default value set to {@code true}.
     * <p>
     * <p>Therefore, the flag provides a level of flexibility such that depending on the cache and environment
     * settings, reporting statistics
     * can be set to false and disabled.</p>
     *
     * @param supportRegistryState true, if the registry is to support registry state
     * @see #sessionCount()
     * @see #serviceTicketCount()
     */
    public void setSupportRegistryState(final boolean supportRegistryState) {
        this.supportRegistryState = supportRegistryState;
    }

    private void configureSecureTransport() {
        final String nullKey = "NULL";

        if (StringUtils.isNotBlank(igniteProperties.getKeyStoreFilePath()) 
                && StringUtils.isNotBlank(igniteProperties.getKeyStorePassword())
                && StringUtils.isNotBlank(igniteProperties.getTrustStoreFilePath()) 
                && StringUtils.isNotBlank(igniteProperties.getTrustStorePassword())) {
            
            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStoreFilePath(igniteProperties.getKeyStoreFilePath());
            sslContextFactory.setKeyStorePassword(igniteProperties.getKeyStorePassword().toCharArray());
            
            if (nullKey.equals(igniteProperties.getTrustStoreFilePath()) 
                    && nullKey.equals(igniteProperties.getTrustStorePassword())) {
                sslContextFactory.setTrustManagers(SslContextFactory.getDisabledTrustManager());
            } else {
                sslContextFactory.setTrustStoreFilePath(igniteProperties.getTrustStoreFilePath());
                sslContextFactory.setTrustStorePassword(igniteProperties.getKeyStorePassword().toCharArray());
            }

            if (StringUtils.isNotBlank(igniteProperties.getKeyAlgorithm())) {
                sslContextFactory.setKeyAlgorithm(igniteProperties.getKeyAlgorithm());
            }
            if (StringUtils.isNotBlank(igniteProperties.getProtocol())) {
                sslContextFactory.setProtocol(igniteProperties.getProtocol());
            }
            if (StringUtils.isNotBlank(igniteProperties.getTrustStoreType())) {
                sslContextFactory.setTrustStoreType(igniteProperties.getTrustStoreType());
            }
            if (StringUtils.isNotBlank(igniteProperties.getKeyStoreType())) {
                sslContextFactory.setKeyStoreType(igniteProperties.getKeyStoreType());
            }
            this.igniteConfiguration.setSslContextFactory(sslContextFactory);
        }
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up Ignite Ticket Registry...");

        configureSecureTransport();

        if (logger.isDebugEnabled()) {
            logger.debug("igniteConfiguration.cacheConfiguration={}", this.igniteConfiguration.getCacheConfiguration());
            logger.debug("igniteConfiguration.getDiscoverySpi={}", this.igniteConfiguration.getDiscoverySpi());
            logger.debug("igniteConfiguration.getSslContextFactory={}", this.igniteConfiguration.getSslContextFactory());
        }

        if (Ignition.state() == IgniteState.STOPPED) {
            this.ignite = Ignition.start(this.igniteConfiguration);
        } else if (Ignition.state() == IgniteState.STARTED) {
            this.ignite = Ignition.ignite();
        }

        this.ticketIgniteCache = this.ignite.getOrCreateCache(igniteProperties.getTicketsCache().getCacheName());

    }

    @Override
    public long sessionCount() {
        return getTickets().stream().filter(t -> t instanceof TicketGrantingTicket).count();
    }

    @Override
    public long serviceTicketCount() {
        return getTickets().stream().filter(t -> t instanceof ServiceTicket).count();
    }

    /**
     * Make sure we shutdown Ignite when the context is destroyed.
     */
    @PreDestroy
    public void shutdown() {
        Ignition.stopAll(true);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("igniteConfiguration", igniteProperties)
                .append("supportRegistryState", this.supportRegistryState)
                .toString();
    }
}
