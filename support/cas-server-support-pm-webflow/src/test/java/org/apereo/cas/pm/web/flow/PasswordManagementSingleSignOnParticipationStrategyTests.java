package org.apereo.cas.pm.web.flow;

import org.apereo.cas.pm.web.flow.actions.BasePasswordManagementActionTests;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.test.MockRequestContext;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategyTests}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@Tag("Webflow")
public class PasswordManagementSingleSignOnParticipationStrategyTests extends BasePasswordManagementActionTests {

    @Test
    public void verifyStrategyWithANonPmRequest() {
        val s = new PasswordManagementSingleSignOnParticipationStrategy(ticketRegistry);
        assertFalse(s.supports(new MockRequestContext()));
    }

    @Test
    public void verifyStrategyWithAnInvalidPmRequest() {
        val s = new PasswordManagementSingleSignOnParticipationStrategy(ticketRegistry);
        val ctx = new MockRequestContext();
        ctx.putRequestParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, "invalidResetToken");

        assertFalse(s.supports(ctx));
    }

    @Test
    public void verifyStrategyWithAValidPmRequest() {
        val s = new PasswordManagementSingleSignOnParticipationStrategy(ticketRegistry);
        val ctx = new MockRequestContext();

        val token = passwordManagementService.createToken("casuser");
        val transientFactory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        val serverPrefix = casProperties.getServer().getPrefix();
        val service = webApplicationServiceFactory.createService(serverPrefix);
        val properties = CollectionUtils.<String, Serializable>wrap(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN, token);
        val ticket = transientFactory.create(service, properties);
        ticketRegistry.addTicket(ticket);
        ctx.putRequestParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN, ticket.getId());

        assertTrue(s.supports(ctx));
        assertFalse(s.isParticipating(ctx));
    }
}
