package org.apereo.cas.monitor;

import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit test for {@link HealthCheckMonitor} class.
 *
 * @author Marvin S. Addison
 * @since 3.5.0
 */
public class HealthCheckMonitorTests {

    @Test
    public void verifyObserveUnknown() throws Exception {
        final HealthCheckMonitor monitor = new HealthCheckMonitor(Collections.emptySet());

        assertEquals(StatusCode.UNKNOWN, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveOk() throws Exception {
        final Set<Monitor> monitors = new HashSet<>();
        monitors.add(new MemoryMonitor());
        monitors.add(newSessionMonitor());
        final HealthCheckMonitor monitor = new HealthCheckMonitor(monitors);
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }

    @Test
    public void verifyObserveWarn() throws Exception {
        final Set<Monitor> monitors = new HashSet<>();
        final MemoryMonitor memoryMonitor = new MemoryMonitor();
        memoryMonitor.setFreeMemoryWarnThreshold(100);
        monitors.add(memoryMonitor);
        monitors.add(newSessionMonitor());
        final HealthCheckMonitor monitor = new HealthCheckMonitor(monitors);
        assertEquals(StatusCode.WARN, monitor.observe().getCode());
    }

    @Test
    public void verifyThrowsUncheckedException() throws Exception {
        final Monitor throwsUnchecked = new Monitor() {
            @Override
            public String getName() {
                return "ThrowsUnchecked";
            }

            @Override
            public Status observe() {
                throw new IllegalStateException("Boogity!");
            }
        };
        final HealthCheckMonitor monitor = new HealthCheckMonitor(Collections.singleton(throwsUnchecked));
        assertEquals(StatusCode.ERROR, monitor.observe().getCode());
    }

    private static SessionMonitor newSessionMonitor() {
        final SessionMonitor sessionMonitor = new SessionMonitor();
        sessionMonitor.setTicketRegistry(new DefaultTicketRegistry());
        return sessionMonitor;
    }
}
