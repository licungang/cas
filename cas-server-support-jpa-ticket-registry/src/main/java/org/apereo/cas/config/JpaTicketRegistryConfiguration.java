package org.apereo.cas.config;

import org.apereo.cas.configuration.model.core.HostProperties;
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.support.JpaLockingStrategy;
import org.apereo.cas.ticket.registry.support.LockingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import static org.apereo.cas.configuration.support.Beans.newEntityManagerFactoryBean;
import static org.apereo.cas.configuration.support.Beans.newHibernateJpaVendorAdapter;
import static org.apereo.cas.configuration.support.Beans.newHickariDataSource;

/**
 * This this {@link JpaTicketRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("jpaTicketRegistryConfiguration")
@EnableConfigurationProperties({JpaTicketRegistryProperties.class, DatabaseProperties.class, HostProperties.class})
public class JpaTicketRegistryConfiguration {

    @Autowired
    private DatabaseProperties databaseProperties;

    @Autowired
    private JpaTicketRegistryProperties jpaTicketRegistryProperties;

    @Autowired
    private HostProperties hostProperties;

    /**
     * Jpa vendor adapter hibernate jpa vendor adapter.
     *
     * @return the hibernate jpa vendor adapter
     */

    public HibernateJpaVendorAdapter ticketJpaVendorAdapter() {
        return newHibernateJpaVendorAdapter(this.databaseProperties);
    }

    /**
     * Jpa packages to scan string [].
     *
     * @return the string [ ]
     */

    public String[] ticketPackagesToScan() {
        return new String[] {
                "org.apereo.cas.ticket",
                "org.apereo.cas.adaptors.jdbc"
        };
    }

    /**
     * Entity manager factory local container.
     *
     * @return the local container entity manager factory bean
     */

    public LocalContainerEntityManagerFactoryBean ticketEntityManagerFactory() {
        return newEntityManagerFactoryBean(
                new JpaConfigDataHolder(
                        ticketJpaVendorAdapter(),
                        "jpaTicketRegistryContext",
                        ticketPackagesToScan(),
                        dataSourceTicket()),
                this.jpaTicketRegistryProperties);
    }

    /**
     * Transaction manager events jpa transaction manager.
     *
     * @param emf the emf
     *
     * @return the jpa transaction manager
     */
    @Bean
    public JpaTransactionManager ticketTransactionManager(@Qualifier("ticketEntityManagerFactory")
                                                          final EntityManagerFactory emf) {
        final JpaTransactionManager mgmr = new JpaTransactionManager();
        mgmr.setEntityManagerFactory(emf);
        return mgmr;
    }

    /**
     * Data source ticket combo pooled data source.
     *
     * @return the combo pooled data source
     */
    @RefreshScope
    @Bean
    public DataSource dataSourceTicket() {
        return newHickariDataSource(this.jpaTicketRegistryProperties);
    }

    @Bean
    @RefreshScope
    public TicketRegistry jpaTicketRegistry() {
        final JpaTicketRegistry bean = new JpaTicketRegistry();
        bean.setLockTgt(this.jpaTicketRegistryProperties.isJpaLockingTgtEnabled());
        return bean;
    }

    @Bean
    public LockingStrategy jpaLockingStrategy() {
        final JpaLockingStrategy bean = new JpaLockingStrategy();
        bean.setApplicationId(this.databaseProperties.getCleaner().getAppid());
        bean.setUniqueId(this.hostProperties.getName());
        return bean;
    }
}
