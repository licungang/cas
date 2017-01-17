package org.apereo.cas.adaptors.azure.web.flow;

import org.apache.commons.lang3.RandomStringUtils;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorTokenCredential;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AzureAuthenticatorGenerateTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AzureAuthenticatorGenerateTokenAction extends AbstractAction {
    private final MultifactorAuthenticationProperties.Azure.AuthenticationModes mode;

    public AzureAuthenticatorGenerateTokenAction(final MultifactorAuthenticationProperties.Azure.AuthenticationModes mode) {
        this.mode = mode;
    }

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        final Integer code = Integer.valueOf(RandomStringUtils.randomNumeric(8));
        final AzureAuthenticatorTokenCredential c = new AzureAuthenticatorTokenCredential();
        c.setToken(code.toString());
        WebUtils.putCredential(requestContext, c);
        requestContext.getFlowScope().put("azureToken", code);

        if (mode == MultifactorAuthenticationProperties.Azure.AuthenticationModes.POUND) {
            return new EventFactorySupport().event(this, "authenticate");
        }
        return success();
    }
}
