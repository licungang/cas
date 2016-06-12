package org.apereo.cas.support.spnego;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Initializes the spnego authentication context.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class SpnegoApplicationContextWrapper extends BaseApplicationContextWrapper {
    @Autowired
    @Qualifier("spnegoHandler")
    private AuthenticationHandler spnegoHandler;

    @Autowired
    @Qualifier("spnegoPrincipalResolver")
    private PrincipalResolver spnegoPrincipalResolver;

    @Autowired
    @Qualifier("successfulHandlerMetaDataPopulator")
    private AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator;

    /**
     * Initialize root application context.
     */
    @PostConstruct
    protected void initializeRootApplicationContext() {
        addAuthenticationHandlerPrincipalResolver(this.spnegoHandler, this.spnegoPrincipalResolver);
        addAuthenticationMetadataPopulator(this.successfulHandlerMetaDataPopulator);
    }

}
