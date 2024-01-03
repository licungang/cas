package org.apereo.cas;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketCatalogConfiguration;
import org.apereo.cas.config.CasWsSecurityTokenTicketComponentSerializationConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderComponentSerializationConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderConfiguration;
import org.apereo.cas.config.CoreWsSecurityIdentityProviderWebflowConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenServiceSamlConfiguration;
import org.apereo.cas.config.CoreWsSecuritySecurityTokenTicketConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseCoreWsSecurityIdentityProviderConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(
    classes = BaseCoreWsSecurityIdentityProviderConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed-idp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS",
        "cas.authn.wsfed-idp.idp.realm-name=CAS",

        "cas.authn.wsfed-idp.sts.signing-keystore-file=classpath:ststrust.jks",
        "cas.authn.wsfed-idp.sts.signing-keystore-password=storepass",

        "cas.authn.wsfed-idp.sts.encryption-keystore-file=classpath:stsencrypt.jks",
        "cas.authn.wsfed-idp.sts.encryption-keystore-password=storepass",

        "cas.authn.wsfed-idp.sts.subject-name-id-format=unspecified",
        "cas.authn.wsfed-idp.sts.encrypt-tokens=true",

        "cas.authn.wsfed-idp.sts.realm.keystore-file=stsrealm_a.jks",
        "cas.authn.wsfed-idp.sts.realm.keystore-password=storepass",
        "cas.authn.wsfed-idp.sts.realm.keystore-alias=realma",
        "cas.authn.wsfed-idp.sts.realm.key-password=realma",
        "cas.authn.wsfed-idp.sts.realm.issuer=CAS"
    })
public abstract class BaseCoreWsSecurityIdentityProviderConfigurationTests {

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CoreSamlConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAutoConfiguration.class,

        CoreWsSecurityIdentityProviderComponentSerializationConfiguration.class,
        CoreWsSecurityIdentityProviderConfiguration.class,
        CoreWsSecurityIdentityProviderWebflowConfiguration.class,

        CasWsSecurityTokenTicketCatalogConfiguration.class,
        CasWsSecurityTokenTicketComponentSerializationConfiguration.class,
        CoreWsSecuritySecurityTokenServiceConfiguration.class,
        CoreWsSecuritySecurityTokenTicketConfiguration.class,
        CoreWsSecuritySecurityTokenServiceSamlConfiguration.class
    })
    static class SharedTestConfiguration {
    }
}
