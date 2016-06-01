package org.apereo.cas.monitor;

import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.mock.MockService;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistryState;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * Unit test for {@link SessionMonitor} class that involves
 * {@link JpaTicketRegistry}.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
@Transactional
public class SessionMonitorJpaTests {

    private static final ExpirationPolicy TEST_EXP_POLICY = new HardTimeoutExpirationPolicy(10000);
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    private TicketRegistry jpaRegistry;

    private SessionMonitor monitor;

    @Before
    public void setUp() {
        final ClassPathXmlApplicationContext ctx = new
            ClassPathXmlApplicationContext("classpath:/jpaSpringContext.xml");
        this.jpaRegistry = ctx.getBean("jpaTicketRegistry", TicketRegistry.class);
        this.monitor = new SessionMonitor();
    }

    @Test
    @Rollback(false)
    public void verifyObserveOkJpaTicketRegistry() throws Exception {
        addTicketsToRegistry(this.jpaRegistry, 5, 5);
        assertEquals(10, this.jpaRegistry.getTickets().size());
        this.monitor.setTicketRegistry((TicketRegistryState) this.jpaRegistry);
        final SessionStatus status = this.monitor.observe();
        assertEquals(5, status.getSessionCount());
        assertEquals(5, status.getServiceTicketCount());
        assertEquals(StatusCode.OK, status.getCode());
    }

    private void addTicketsToRegistry(final TicketRegistry registry, final int tgtCount, final int stCount) {
        TicketGrantingTicketImpl ticket = null;
        for (int i = 0; i < tgtCount; i++) {
            ticket = new TicketGrantingTicketImpl(
                    GENERATOR.getNewTicketId("TGT"),
                    TestUtils.getAuthentication(),
                    TEST_EXP_POLICY);
            registry.addTicket(ticket);
        }

        if (ticket != null) {
          for (int i = 0; i < stCount; i++) {
              registry.addTicket(ticket.grantServiceTicket(
                      GENERATOR.getNewTicketId("ST"),
                      new MockService("junit"),
                      TEST_EXP_POLICY,
                      null,
                      true));
          }
        }
    }
}
