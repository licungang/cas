package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ProxyingPrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.generic.AcceptAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jaas.JaasAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link CasCoreAuthenticationHandlersConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationHandlersConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureBefore(CasCoreAuthenticationConfiguration.class)
public class CasCoreAuthenticationHandlersConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;
    
    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;
    
    @Autowired
    @Qualifier("supportsTrustStoreSslSocketFactoryHttpClient")
    private HttpClient supportsTrustStoreSslSocketFactoryHttpClient;

    @Autowired(required = false)
    @Qualifier("acceptPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration acceptPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("jaasPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration jaasPasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @ConditionalOnMissingBean(name = "jaasPrincipalFactory")
    @Bean
    public PrincipalFactory jaasPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler jaasAuthenticationHandler() {
        final JaasAuthenticationProperties jaas = casProperties.getAuthn().getJaas();
        final JaasAuthenticationHandler h = new JaasAuthenticationHandler(jaas.getName(), servicesManager);

        h.setKerberosKdcSystemProperty(jaas.getKerberosKdcSystemProperty());
        h.setKerberosRealmSystemProperty(jaas.getKerberosRealmSystemProperty());
        h.setRealm(jaas.getRealm());
        h.setPasswordEncoder(Beans.newPasswordEncoder(jaas.getPasswordEncoder()));

        if (jaasPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(jaasPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(jaas.getPrincipalTransformation()));

        h.setPrincipalFactory(jaasPrincipalFactory());
        return h;
    }

    @Bean
    public AuthenticationHandler proxyAuthenticationHandler() {
        final HttpBasedServiceCredentialsAuthenticationHandler h = new HttpBasedServiceCredentialsAuthenticationHandler(servicesManager);
        h.setHttpClient(supportsTrustStoreSslSocketFactoryHttpClient);
        h.setPrincipalFactory(proxyPrincipalFactory());
        return h;
    }

    @ConditionalOnMissingBean(name = "proxyPrincipalFactory")
    @Bean
    public PrincipalFactory proxyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public PrincipalResolver proxyPrincipalResolver() {
        final ProxyingPrincipalResolver p = new ProxyingPrincipalResolver();
        p.setPrincipalFactory(proxyPrincipalFactory());
        return p;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler acceptUsersAuthenticationHandler() {
        final AcceptAuthenticationProperties accept = casProperties.getAuthn().getAccept();
        final AcceptUsersAuthenticationHandler h = new AcceptUsersAuthenticationHandler(accept.getName(), servicesManager, getParsedUsers());
        h.setPasswordEncoder(Beans.newPasswordEncoder(accept.getPasswordEncoder()));
        if (acceptPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(acceptPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(accept.getPrincipalTransformation()));
        h.setPrincipalFactory(acceptUsersPrincipalFactory());
        return h;
    }

    @ConditionalOnMissingBean(name = "acceptUsersPrincipalFactory")
    @Bean
    public PrincipalFactory acceptUsersPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }
    private Map<String, String> getParsedUsers() {
        final Pattern pattern = Pattern.compile("::");

        final String usersProperty = casProperties.getAuthn().getAccept().getUsers();

        if (StringUtils.isNotBlank(usersProperty) && usersProperty.contains(pattern.pattern())) {
            return Stream.of(usersProperty.split(","))
                    .map(pattern::split)
                    .collect(Collectors.toMap(userAndPassword -> userAndPassword[0], userAndPassword -> userAndPassword[1]));
        }
        return Collections.emptyMap();
    }

    @PostConstruct
    public void initializeAuthenticationHandler() {
        this.authenticationHandlersResolvers.put(proxyAuthenticationHandler(), proxyPrincipalResolver());
        if (StringUtils.isNotBlank(casProperties.getAuthn().getJaas().getRealm())) {
            authenticationHandlersResolvers.put(jaasAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }
}
