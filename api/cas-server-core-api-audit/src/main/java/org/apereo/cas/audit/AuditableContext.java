package org.apereo.cas.audit;

import lombok.Builder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AuditableContext}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Builder
public class AuditableContext {

    /**
     * Service.
     */
    @Builder.Default
    private Service service = null;

    /**
     * RegisteredService.
     */
    @Builder.Default
    private RegisteredService registeredService = null;

    /**
     * Authentication.
     */
    @Builder.Default
    private Authentication authentication = null;

    /**
     * ServiceTicket.
     */
    @Builder.Default
    private ServiceTicket serviceTicket = null;

    /**
     * AuthenticationResult.
     */
    @Builder.Default
    private AuthenticationResult authenticationResult = null;

    /**
     * TicketGrantingTicket.
     */
    @Builder.Default
    private TicketGrantingTicket ticketGrantingTicket = null;

    /**
     * retrievePrincipalAttributesFromReleasePolicy.
     */
    @Builder.Default
    private Boolean retrievePrincipalAttributesFromReleasePolicy = null;

    /**
     * Properties.
     */
    @Builder.Default
    private Map<String, Object> properties = new LinkedHashMap<>();

    /**
     * Get.
     *
     * @return optional service
     */
    public Optional<Service> getService() {
        return Optional.ofNullable(service);
    }

    /**
     * Get.
     *
     * @return optional registered service
     */
    public Optional<RegisteredService> getRegisteredService() {
        return Optional.ofNullable(registeredService);
    }

    /**
     * Get.
     *
     * @return optional authentication
     */
    public Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(authentication);
    }

    /**
     * Get.
     *
     * @return optional service ticket
     */
    public Optional<ServiceTicket> getServiceTicket() {
        return Optional.ofNullable(serviceTicket);
    }

    /**
     * Get.
     *
     * @return optional authentication result
     */
    public Optional<AuthenticationResult> getAuthenticationResult() {
        return Optional.ofNullable(authenticationResult);
    }

    /**
     * Get.
     *
     * @return optional tgt
     */
    public Optional<TicketGrantingTicket> getTicketGrantingTicket() {
        return Optional.ofNullable(ticketGrantingTicket);
    }

    /**
     * Get.
     *
     * @return optional attribute retrieval policy flag
     */
    public Optional<Boolean> getRetrievePrincipalAttributesFromReleasePolicy() {
        return Optional.ofNullable(retrievePrincipalAttributesFromReleasePolicy);
    }

    /**
     * Get.
     *
     * @return optional properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
}
