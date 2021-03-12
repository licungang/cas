package org.apereo.cas.oidc;

import org.apereo.cas.oidc.claims.mapping.OidcAttributeToScopeClaimMapper;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link OidcConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@SuperBuilder
public class OidcConfigurationContext extends OAuth20ConfigurationContext {
    private final OidcAttributeToScopeClaimMapper attributeToScopeClaimMapper;

}
