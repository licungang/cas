package org.apereo.cas.oidc.validator.authorization;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

/**
 * This is {@link OidcCodeIdTokenResponseTypeAuthorizationRequestValidator}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
public class OidcCodeIdTokenResponseTypeAuthorizationRequestValidator extends OidcCodeTokenResponseTypeAuthorizationRequestValidator {
    public OidcCodeIdTokenResponseTypeAuthorizationRequestValidator(final ServicesManager servicesManager,
                                                                    final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.CODE_ID_TOKEN;
    }
}
