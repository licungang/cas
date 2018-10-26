package org.apereo.cas.configuration.model.support.jdbc.authn;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link BindJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
@Getter
@Setter
public class BindJdbcAuthenticationProperties extends BaseJdbcAuthenticationProperties {
    private static final long serialVersionUID = 4268982716707687796L;
}
