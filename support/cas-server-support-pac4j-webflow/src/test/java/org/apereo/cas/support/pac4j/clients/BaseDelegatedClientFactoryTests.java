package org.apereo.cas.support.pac4j.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactory;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link BaseDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
public abstract class BaseDelegatedClientFactoryTests {
    @Autowired
    @Qualifier(CasSSLContext.BEAN_NAME)
    protected CasSSLContext casSslContext;

    @Autowired
    @Qualifier("pac4jDelegatedClientFactory")
    protected DelegatedClientFactory delegatedClientFactory;
}

