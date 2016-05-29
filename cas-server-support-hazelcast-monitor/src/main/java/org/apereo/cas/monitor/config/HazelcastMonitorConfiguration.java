package org.apereo.cas.monitor.config;

import org.apereo.cas.monitor.HazelcastMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link HazelcastMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("hazelcastMonitorConfiguration")
public class HazelcastMonitorConfiguration {
    
    @Bean
    public Monitor hazelcastMonitor() {
        return new HazelcastMonitor();
    }
}
