package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.hz.HazelcastConfigurationFactory;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.impl.HazelcastInstanceFactory;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.session.HazelcastSessionProperties;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.session.MapSession;
import org.springframework.session.hazelcast.Hazelcast4IndexedSessionRepository;
import org.springframework.session.hazelcast.HazelcastSessionSerializer;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

import java.time.Duration;

/**
 * This is {@link HazelcastSessionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableHazelcastHttpSession
@EnableConfigurationProperties({CasConfigurationProperties.class,
    SessionProperties.class, HazelcastSessionProperties.class, ServerProperties.class})
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.SessionManagement, module = "hazelcast")
@AutoConfiguration
public class HazelcastSessionConfiguration {

    /**
     * Hazelcast instance that is used by the spring session
     * repository to broadcast session events. The name
     * of this bean must be left untouched.
     *
     * @param casProperties              the cas properties
     * @param hazelcastSessionProperties the hazelcast session properties
     * @param sessionProperties          the session properties
     * @param serverProperties           the server properties
     * @return the hazelcast instance
     */
    @Bean(destroyMethod = "shutdown")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HazelcastInstance hazelcastInstance(final CasConfigurationProperties casProperties,
                                               final HazelcastSessionProperties hazelcastSessionProperties,
                                               final SessionProperties sessionProperties,
                                               final ServerProperties serverProperties) {
        val hz = casProperties.getWebflow().getSession().getHazelcast();
        val config = HazelcastConfigurationFactory.build(hz);
        val serializerConfig = new SerializerConfig();
        serializerConfig.setImplementation(new HazelcastSessionSerializer()).setTypeClass(MapSession.class);
        config.getSerializationConfig().addSerializerConfig(serializerConfig);

        val duration = (Duration) ObjectUtils.defaultIfNull(sessionProperties.getTimeout(),
            serverProperties.getServlet().getSession().getTimeout());

        val hazelcastInstance = HazelcastInstanceFactory.getOrCreateHazelcastInstance(config);
        val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hz,
            hazelcastSessionProperties.getMapName(), duration.toSeconds());
        if (mapConfig instanceof MapConfig) {
            val finalConfig = (MapConfig) mapConfig;
            val attributeConfig = new AttributeConfig();
            attributeConfig.setName(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE);
            attributeConfig.setExtractorClassName(HazelcastSessionPrincipalNameExtractor.class.getName());
            finalConfig.addAttributeConfig(attributeConfig);
            val indexConfig = new IndexConfig();
            indexConfig.addAttribute(Hazelcast4IndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE);
            finalConfig.addIndexConfig(indexConfig);
        }
        HazelcastConfigurationFactory.setConfigMap(mapConfig, hazelcastInstance.getConfig());
        return hazelcastInstance;
    }
}
