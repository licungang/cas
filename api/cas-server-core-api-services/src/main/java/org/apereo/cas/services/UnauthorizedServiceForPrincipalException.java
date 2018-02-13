package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link UnauthorizedServiceForPrincipalException}
 * thrown when an attribute is missing from principal
 * attribute release policy that would otherwise grant access
 * to the service that is requesting authentication.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class UnauthorizedServiceForPrincipalException extends UnauthorizedServiceException {

    private static final long serialVersionUID = 8909291297815558561L;

    /** The code description. */
    private static final String CODE = "service.not.authorized.missing.attr";

    private String principal;

    private String targetServiceId;

    private RegisteredService registeredService;

    /**
     * Instantiates a new unauthorized sso service exception.
     */
    public UnauthorizedServiceForPrincipalException() {
        super(CODE, StringUtils.EMPTY);
    }

    /**
     * Instantiates a new unauthorized sso service exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnauthorizedServiceForPrincipalException(final String message,
                                                    final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new unauthorized sso service exception.
     *
     * @param message the message
     */
    public UnauthorizedServiceForPrincipalException(final String message) {
        super(message);
    }

    public UnauthorizedServiceForPrincipalException(final String message,
                                                    final String principalId,
                                                    final String targetServiceId,
                                                    final RegisteredService registeredService) {
        super(message);
        this.principal = principalId;
        this.targetServiceId = targetServiceId;
        this.registeredService = registeredService;
    }
}
