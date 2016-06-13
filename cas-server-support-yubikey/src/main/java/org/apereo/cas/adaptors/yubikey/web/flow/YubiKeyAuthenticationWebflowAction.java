package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyAuthenticationWebflowAction extends AbstractAction {
    private CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return this.yubikeyAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }

    public CasWebflowEventResolver getYubikeyAuthenticationWebflowEventResolver() {
        return yubikeyAuthenticationWebflowEventResolver;
    }

    public void setYubikeyAuthenticationWebflowEventResolver(final CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver) {
        this.yubikeyAuthenticationWebflowEventResolver = yubikeyAuthenticationWebflowEventResolver;
    }
}
