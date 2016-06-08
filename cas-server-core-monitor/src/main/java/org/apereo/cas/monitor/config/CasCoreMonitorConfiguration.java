package org.apereo.cas.monitor.config;

import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.monitor.HealthCheckMonitor;
import org.apereo.cas.monitor.MemoryMonitor;
import org.apereo.cas.monitor.Monitor;
import org.apereo.cas.monitor.SessionMonitor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;

/**
 * This is {@link CasCoreMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreMonitorConfiguration")
@EnableConfigurationProperties(MonitorProperties.class)
public class CasCoreMonitorConfiguration {

    //Spring 4.2 does not support injecting generic collections via @Autowired. 4.3 will fix that.
    @Resource(name="monitorsList")
    private Collection<Monitor> monitors = Collections.emptySet();

    @Autowired
    private MonitorProperties monitorProperties;
    
    @Bean
    public Monitor healthCheckMonitor() {
        final HealthCheckMonitor bean = new HealthCheckMonitor();
        bean.setMonitors(this.monitors);
        return bean;
    }

    @RefreshScope
    @Bean
    public Monitor memoryMonitor() {
        final MemoryMonitor bean = new MemoryMonitor();
        bean.setFreeMemoryWarnThreshold(this.monitorProperties.getFreeMemThreshold());
        return bean;
    }

    @RefreshScope
    @Bean
    @Autowired
    public Monitor sessionMonitor(@Qualifier("ticketRegistry") final TicketRegistry ticketRegistry) {
        final SessionMonitor bean = new SessionMonitor();
        bean.setTicketRegistry(ticketRegistry);
        bean.setServiceTicketCountWarnThreshold(this.monitorProperties.getSt().getWarn().getThreshold());
        bean.setSessionCountWarnThreshold(this.monitorProperties.getTgt().getWarn().getThreshold());
        return bean;
    }
}
