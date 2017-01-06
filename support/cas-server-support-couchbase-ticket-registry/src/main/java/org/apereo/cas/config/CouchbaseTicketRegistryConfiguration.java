package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.couchbase.ticketregistry.CouchbaseTicketRegistryProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.CouchbaseTicketRegistry;
import org.apereo.cas.ticket.registry.NoOpLockingStrategy;
import org.apereo.cas.ticket.registry.NoOpTicketRegistryCleaner;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistryCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * This is {@link CouchbaseTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("couchbaseTicketRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseTicketRegistryConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @RefreshScope
    @Bean
    public CouchbaseClientFactory ticketRegistryCouchbaseClientFactory() {
        final CouchbaseTicketRegistryProperties cb = casProperties.getTicket().getRegistry().getCouchbase();
        final Set<String> nodes = StringUtils.commaDelimitedListToSet(cb.getNodeSet());
        return new CouchbaseClientFactory(nodes, cb.getBucket(), cb.getPassword(), cb.getTimeout());
    }

    @RefreshScope
    @Bean(name = {"couchbaseTicketRegistry", "ticketRegistry"})
    public TicketRegistry couchbaseTicketRegistry() {
        final CouchbaseTicketRegistry c = new CouchbaseTicketRegistry();
        c.setCouchbaseClientFactory(ticketRegistryCouchbaseClientFactory());
        c.setCipherExecutor(Beans.newTicketRegistryCipherExecutor(
                casProperties.getTicket().getRegistry().getCouchbase().getCrypto()
        ));
        return c;
    }

    @Bean
    public TicketRegistryCleaner ticketRegistryCleaner() {
        return new NoOpTicketRegistryCleaner(new NoOpLockingStrategy(), logoutManager, couchbaseTicketRegistry(), false);
    }
}
