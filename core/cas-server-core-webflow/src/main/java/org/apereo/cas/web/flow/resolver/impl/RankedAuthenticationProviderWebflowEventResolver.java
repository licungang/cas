package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RankedAuthenticationProviderWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RankedAuthenticationProviderWebflowEventResolver extends AbstractCasWebflowEventResolver {

    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    private final AuthenticationContextValidator authenticationContextValidator;

    public RankedAuthenticationProviderWebflowEventResolver(final AuthenticationContextValidator authenticationContextValidator,
                                                            final CasDelegatingWebflowEventResolver casDelegatingWebflowEventResolver) {
        this.authenticationContextValidator = authenticationContextValidator;
        this.initialAuthenticationAttemptWebflowEventResolver = casDelegatingWebflowEventResolver;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final String tgt = WebUtils.getTicketGrantingTicketId(context);
        final RegisteredService service = WebUtils.getRegisteredService(context);

        if (service == null) {
            logger.debug("No service is available to determine event for principal");
            return resumeFlow();
        }

        if (StringUtils.isBlank(tgt)) {
            logger.trace("TGT is blank; proceed with flow normally.");
            return resumeFlow();
        }
        final Authentication authentication = this.ticketRegistrySupport.getAuthenticationFrom(tgt);
        if (authentication == null) {
            logger.trace("TGT has no authentication and is blank; proceed with flow normally.");
            return resumeFlow();
        }

        final AuthenticationResultBuilder builder = this.authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication);
        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authentication, context);

        final Event event = this.initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        if (event == null) {
            logger.trace("Request does not indicate a requirement for authentication policy; proceed with flow normally.");
            return resumeFlow();
        }

        final String id = event.getId();

        if (id.equals(CasWebflowConstants.TRANSITION_ID_ERROR)
                || id.equals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE)
                || id.equals(CasWebflowConstants.TRANSITION_ID_SUCCESS)) {
            logger.debug("Returning webflow event as {}", id);
            return ImmutableSet.of(event);
        }

        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result = this.authenticationContextValidator.validate(authentication, id, service);

        if (result.getKey()) {
            logger.debug("Authentication context is successfully validated by {} for service {}", id, service);
            return resumeFlow();
        }

        if (result.getValue().isPresent()) {
            return ImmutableSet.of(validateEventIdForMatchingTransitionInContext(id, context,
                    buildEventAttributeMap(authentication.getPrincipal(), service, result.getValue().get())));
        }
        logger.warn("The authentication context cannot be satisfied and the requested event {} is unrecognized", id);
        return ImmutableSet.of(new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR));
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    private Set<Event> resumeFlow() {
        return ImmutableSet.of(new EventFactorySupport().success(this));
    }
}
