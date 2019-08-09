package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCouchDbCoreConfiguration;
import org.apereo.cas.config.CouchDbServiceRegistryConfiguration;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link CouchDbServiceRegistryTests}.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCouchDbCoreConfiguration.class,
    CasCoreServicesConfiguration.class,
    CouchDbServiceRegistryConfiguration.class
},
    properties = {
        "cas.serviceRegistry.couchDb.username=cas",
        "cas.serviceRegistry.couchDb.password=password"
    })
@Tag("CouchDb")
@EnabledIfContinuousIntegration
public class CouchDbServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("couchDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }
}
