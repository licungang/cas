package org.apereo.cas.oidc.validator.authorization;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

/**
 * This is {@link OidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
public class OidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator extends OidcIdTokenResponseTypeAuthorizationRequestValidator {
    public OidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator(final ServicesManager servicesManager,
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
        return OAuth20ResponseTypes.IDTOKEN_TOKEN;
    }
}
