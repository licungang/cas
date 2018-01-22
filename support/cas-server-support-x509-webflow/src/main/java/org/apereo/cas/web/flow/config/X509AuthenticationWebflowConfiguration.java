package org.apereo.cas.web.flow.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.extractcert.ExtractX509Certificate;
import org.apereo.cas.web.extractcert.ExtractX509CertificateFromHeader;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.X509CertificateCredentialsNonInteractiveAction;
import org.apereo.cas.web.flow.X509WebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link X509AuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("x509AuthenticationWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class X509AuthenticationWebflowConfiguration {

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired(required = false)
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired(required = false)
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "x509WebflowConfigurer")
    @ConditionalOnBean(name = "defaultWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer x509WebflowConfigurer() {
        final CasWebflowConfigurer w = new X509WebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        w.initialize();
        return w;
    }

    @Bean
    public Action x509Check() {
        final boolean extractCertFromRequest = casProperties.getAuthn().getX509().isExtractCert();
        return new X509CertificateCredentialsNonInteractiveAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver,
            adaptiveAuthenticationPolicy,
            extractCertFromRequest);
    }

    @ConditionalOnMissingBean(name = "x509ExtractSSLCertificate")
    @Bean
    public ExtractX509Certificate x509ExtractSSLCertificate() {
        final String sslHeaderName = casProperties.getAuthn().getX509().getSslHeaderName();
        final ExtractX509Certificate esc = new ExtractX509CertificateFromHeader(sslHeaderName);
        return esc;
    }
}
