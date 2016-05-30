package org.apereo.cas.config;

import org.apereo.cas.adaptors.yubikey.YubiKeyApplicationContextWrapper;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationHandler;
import org.apereo.cas.adaptors.yubikey.YubiKeyAuthenticationMetaDataPopulator;
import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.yubikey.web.flow.YubiKeyMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import javax.annotation.Resource;

/**
 * This is {@link YubiKeyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("yubikeyConfiguration")
public class YubiKeyConfiguration {

    @Value("${cas.mfa.yubikey.client.id:}") 
    private Integer clientId;
    
    @Value("${cas.mfa.yubikey.secret.key:}") 
    private String secretKey;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Resource(name="builder")
    private FlowBuilderServices builder;
    
    @Bean
    public FlowDefinitionRegistry yubikeyFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.builder);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-yubikey/*-webflow.xml");
        return builder.build();
    }
    
    @Bean
    public BaseApplicationContextWrapper yubiKeyApplicationContextWrapper() {
        return new YubiKeyApplicationContextWrapper();
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler yubikeyAuthenticationHandler() {
        return new YubiKeyAuthenticationHandler(this.clientId, this.secretKey);
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator yubikeyAuthenticationMetaDataPopulator() {
        return new YubiKeyAuthenticationMetaDataPopulator();
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider yubikeyAuthenticationProvider() {
        return new YubiKeyMultifactorAuthenticationProvider();
    }

    @Bean
    public Action yubikeyAuthenticationWebflowAction() {
        return new YubiKeyAuthenticationWebflowAction();
    }

    @Bean
    public CasWebflowConfigurer yubikeyAuthenticationWebflowEventResolver() {
        return new YubiKeyMultifactorWebflowConfigurer();
    }

    @Bean
    public CasWebflowEventResolver yubikeyMultifactorWebflowConfigurer() {
        return new YubiKeyAuthenticationWebflowEventResolver();
    }
}
