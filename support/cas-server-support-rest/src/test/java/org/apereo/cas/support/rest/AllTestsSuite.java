package org.apereo.cas.support.rest;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({
    TicketGrantingTicketResourceTests.class,
    ServiceTicketResourceTests.class,
    TicketStatusResourceTests.class,
    UserAuthenticationResourceTests.class
})
public class AllTestsSuite {
}
