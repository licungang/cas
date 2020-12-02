package org.apereo.cas.support.inwebo.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.inwebo.service.ConsoleAdmin;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.SSLUtil;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

/**
 * The Inwebo services configuration.
 *
 * @author Jerome LELEU
 * @since 6.3.0
 */
@Configuration("inweboConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class InweboConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "marshaller")
    public Jaxb2Marshaller marshaller() {
        val marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(this.getClass().getPackageName().replaceAll("config", "service.soap"));
        return marshaller;
    }

    @Bean
    @ConditionalOnMissingBean(name = "consoleAdmin")
    @RefreshScope
    public ConsoleAdmin consoleAdmin() {
        val client = new ConsoleAdmin();
        client.setDefaultUri("https://api.myinwebo.com/v2/services/ConsoleAdmin");
        client.setMarshaller(marshaller());
        client.setUnmarshaller(marshaller());

        try {
            val messageSender = new HttpsUrlConnectionMessageSender();
            messageSender.setKeyManagers(SSLUtil.buildKeystore(casProperties.getAuthn().getMfa().getInwebo()).getKeyManagers());
            val tmFactory = TrustManagerFactory.getInstance("PKIX");
            tmFactory.init((KeyStore) null);
            messageSender.setTrustManagers(tmFactory.getTrustManagers());
            client.setMessageSender(messageSender);
        } catch (final Exception e) {
            throw new RuntimeException("Cannot initialize ConsoleAdmin", e);
        }

        return client;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboService")
    @RefreshScope
    public InweboService inweboService() {
        return new InweboService(casProperties, consoleAdmin());
    }
}
