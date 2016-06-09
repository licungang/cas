package org.apereo.cas.adaptors.ldap.services.config;

import org.apereo.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapRegisteredServiceMapper;
import org.apereo.cas.adaptors.ldap.services.LdapServiceRegistryDao;
import org.apereo.cas.configuration.model.support.ldap.serviceregistry.LdapServiceRegistryProperties;
import org.apereo.cas.services.ServiceRegistryDao;
import org.ldaptive.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 * This is {@link LdapServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ldapServiceRegistryConfiguration")
public class LdapServiceRegistryConfiguration {

    @Autowired
    private LdapServiceRegistryProperties properties;


    @Nullable
    @Autowired(required = false)
    @Qualifier("ldapServiceRegistryConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Bean
    @RefreshScope
    public LdapRegisteredServiceMapper ldapServiceRegistryMapper() {
        return new DefaultLdapRegisteredServiceMapper();
    }

    @Bean
    @RefreshScope
    public ServiceRegistryDao ldapServiceRegistryDao() {
        final LdapServiceRegistryDao r = new LdapServiceRegistryDao();

        r.setConnectionFactory(connectionFactory);
        r.setLdapServiceMapper(ldapServiceRegistryMapper());
        r.setBaseDn(properties.getBaseDn());
        
        return r;
    }

}
