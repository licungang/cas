package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.session.JdbcSessionDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.session.JdbcSessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import javax.sql.DataSource;

/**
 * This is {@link CasJdbcSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ConditionalOnFeaturesEnabled({
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.JDBC),
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement)
})
@EnableJdbcHttpSession
@EnableConfigurationProperties({CasConfigurationProperties.class, JdbcSessionProperties.class})
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
@AutoConfiguration
public class CasJdbcSessionConfiguration {
    @Bean
    @ConditionalOnMissingBean
    JdbcSessionDataSourceScriptDatabaseInitializer jdbcSessionDataSourceInitializer(
        @SpringSessionDataSource
        final ObjectProvider<DataSource> sessionDataSource,
        final ObjectProvider<DataSource> dataSource,
        final JdbcSessionProperties properties) {
        return new JdbcSessionDataSourceScriptDatabaseInitializer(
            sessionDataSource.getIfAvailable(dataSource::getObject), properties);
    }
}
