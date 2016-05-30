package org.apereo.cas.support.wsfederation;


import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Initializes the CAS root servlet context to make sure
 * ADFS validation can be activated and authentication handlers injected.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WsFedApplicationContextWrapper extends BaseApplicationContextWrapper {

    @Resource(name="adfsAuthNHandler")
    private AuthenticationHandler adfsAuthNHandler;

    @Resource(name="adfsPrincipalResolver")
    private PrincipalResolver adfsPrincipalResolver;

    @Value("${cas.wsfed.idp.attribute.resolver.enabled:true}")
    private boolean useResolver;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        if (!this.useResolver) {
            addAuthenticationHandler(this.adfsAuthNHandler);
        } else {
            addAuthenticationHandlerPrincipalResolver(this.adfsAuthNHandler, this.adfsPrincipalResolver);
        }
    }
}

