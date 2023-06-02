package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.logging.GoogleCloudLoggingWebInterceptor;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Nonnull;

/**
 * This is {@link GoogleCloudLoggingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Logging, module = "gcp")
public class GoogleCloudLoggingConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "googleCloudLoggingInterceptor")
    public HandlerInterceptor googleCloudLoggingInterceptor() {
        return new GoogleCloudLoggingWebInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "googleCloudLoggingWebMvcConfigurer")
    public WebMvcConfigurer googleCloudLoggingWebMvcConfigurer(
        @Qualifier("googleCloudLoggingInterceptor")
        final ObjectProvider<HandlerInterceptor> googleCloudLoggingInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(
                @Nonnull
                final InterceptorRegistry registry) {
                registry.addInterceptor(new RefreshableHandlerInterceptor(googleCloudLoggingInterceptor)).addPathPatterns("/**");
            }
        };
    }

    @ConditionalOnMissingBean(name = "googleCloudWebflowExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExecutionPlanConfigurer googleCloudWebflowExecutionPlanConfigurer(
        @Qualifier("googleCloudLoggingInterceptor")
        final ObjectProvider<HandlerInterceptor> googleCloudLoggingInterceptor) {
        return plan -> plan.registerWebflowInterceptor(new RefreshableHandlerInterceptor(googleCloudLoggingInterceptor));
    }

}
