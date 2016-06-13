package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.JRadiusServerImpl;
import org.apereo.cas.adaptors.radius.RadiusClientFactory;
import org.apereo.cas.adaptors.radius.RadiusProtocol;
import org.apereo.cas.adaptors.radius.authentication.handler.support.RadiusAuthenticationHandler;
import org.apereo.cas.adaptors.radius.web.RadiusApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This this {@link RadiusConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("radiusConfiguration")
public class RadiusConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("radiusPasswordEncoder")
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    @Qualifier("radiusPrincipalNameTransformer")
    private PrincipalNameTransformer principalNameTransformer;

    @Autowired(required = false)
    @Qualifier("radiusPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration passwordPolicyConfiguration;


    /**
     * Radius server j radius server.
     *
     * @return the j radius server
     */
    @RefreshScope
    @Bean
    public JRadiusServerImpl radiusServer() {

        final RadiusClientFactory factory = new RadiusClientFactory();
        factory.setAccountingPort(casProperties.getAuthn().getRadius().getClient().getAccountingPort());
        factory.setAuthenticationPort(casProperties.getAuthn().getRadius().getClient().getAuthenticationPort());
        factory.setInetAddress(casProperties.getAuthn().getRadius().getClient().getInetAddress());
        factory.setSharedSecret(casProperties.getAuthn().getRadius().getClient().getSharedSecret());
        factory.setSocketTimeout(casProperties.getAuthn().getRadius().getClient().getSocketTimeout());

        final RadiusProtocol protocol = RadiusProtocol.valueOf(
                casProperties.getAuthn().getRadius().getServer().getProtocol());

        final JRadiusServerImpl impl = new JRadiusServerImpl(protocol, factory);
        impl.setRetries(casProperties.getAuthn().getRadius().getServer().getRetries());
        impl.setNasIdentifier(casProperties.getAuthn().getRadius().getServer().getNasIdentifier());
        impl.setNasPort(casProperties.getAuthn().getRadius().getServer().getNasPort());
        impl.setNasPortId(casProperties.getAuthn().getRadius().getServer().getNasPortId());
        impl.setNasRealPort(casProperties.getAuthn().getRadius().getServer().getNasRealPort());
        impl.setNasIpAddress(casProperties.getAuthn().getRadius().getServer().getNasIpAddress());
        impl.setNasIpv6Address(casProperties.getAuthn().getRadius().getServer().getNasIpv6Address());

        return impl;
    }

    /**
     * Radius servers list.
     *
     * @return the list
     */
    @RefreshScope
    @Bean
    public List radiusServers() {
        final List<JRadiusServerImpl> list = new ArrayList<>();
        list.add(radiusServer());
        return list;
    }

    @Bean
    public AuthenticationHandler radiusAuthenticationHandler() {
        final RadiusAuthenticationHandler h = new RadiusAuthenticationHandler();

        h.setFailoverOnAuthenticationFailure(casProperties.getAuthn().getRadius().isFailoverOnAuthenticationFailure());
        h.setFailoverOnException(casProperties.getAuthn().getRadius().isFailoverOnException());
        h.setServers(radiusServers());

        if (passwordEncoder != null) {
            h.setPasswordEncoder(passwordEncoder);
        }
        if (passwordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(passwordPolicyConfiguration);
        }
        if (principalNameTransformer != null) {
            h.setPrincipalNameTransformer(principalNameTransformer);
        }
        return h;
    }

    @Bean
    public BaseApplicationContextWrapper radiusApplicationContextWrapper() {
        final RadiusApplicationContextWrapper w = new RadiusApplicationContextWrapper();
        w.setAuthenticationHandler(radiusAuthenticationHandler());
        return w;
    }
}

  
