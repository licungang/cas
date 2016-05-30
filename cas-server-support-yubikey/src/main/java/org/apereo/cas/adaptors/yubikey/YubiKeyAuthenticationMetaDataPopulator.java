package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

/**
 * This is {@link YubiKeyAuthenticationMetaDataPopulator} which inserts the
 * yubikey MFA provider id into the final authentication object.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @Value("${cas.mfa.authn.ctx.attribute:authnContextClass}")
    private String authenticationContextAttribute;

    @Resource(name="yubikeyAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;


    @Resource(name="yubikeyAuthenticationProvider")
    private MultifactorAuthenticationProvider provider;

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        if (builder.hasAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
                obj -> obj.toString().equals(this.authenticationHandler.getName()))) {
            builder.mergeAttribute(this.authenticationContextAttribute, this.provider.getId());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return this.authenticationHandler.supports(credential);
    }
}

