package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@TestPropertySource(properties = {
    "cas.sso.allow-missing-service-parameter=false",
    "cas.authn.policy.source-selection-enabled=true",
    "cas.authn.accept.users=casuser::Mellon"
})
@Tag("Webflow")
public class InitialFlowSetupActionSsoTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("initialFlowSetupAction")
    private Action action;

    @Test
    public void disableFlowIfNoService() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertThrows(NoSuchFlowExecutionException.class, () -> this.action.execute(context));
        assertEquals(1, WebUtils.getAvailableAuthenticationHandleNames(context).size());
    }
}
