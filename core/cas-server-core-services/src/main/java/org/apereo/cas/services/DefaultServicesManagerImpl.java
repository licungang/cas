package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.events.CasRegisteredServiceDeletedEvent;
import org.apereo.cas.support.events.CasRegisteredServiceSavedEvent;
import org.apereo.cas.support.events.CasRegisteredServicesRefreshEvent;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link ServicesManager} interface. 
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class DefaultServicesManagerImpl implements ServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServicesManagerImpl.class);

    private ServiceRegistryDao serviceRegistryDao;

    private ServiceFactory serviceFactory;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Map<Long, RegisteredService> services = new ConcurrentHashMap<>();
    private Set<RegisteredService> orderedServices;

    public DefaultServicesManagerImpl() {
    }

    /**
     * Instantiates a new default services manager impl.
     *
     * @param serviceRegistryDao the service registry dao
     */
    public DefaultServicesManagerImpl(final ServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;
    }

    public void setServiceRegistryDao(final ServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;
    }

    public void setServiceFactory(final ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    @Audit(action = "DELETE_SERVICE", actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService delete(final long id) {
        final RegisteredService service = findServiceBy(id);
        if (service != null) {
            this.serviceRegistryDao.delete(service);
            this.services.remove(id);
            this.orderedServices = convertToTreeSet();
            publishEvent(new CasRegisteredServiceDeletedEvent(this, service));
        }
        return service;
    }

    @Override
    public RegisteredService findServiceBy(final Service service) {
        return orderedServices.stream().filter(r -> r.matches(service)).findFirst().orElse(null);
    }

    @Override
    public Collection<RegisteredService> findServiceBy(final Predicate<RegisteredService> predicate) {
        return orderedServices.stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);

        try {
            return r == null ? null : r.clone();
        } catch (final CloneNotSupportedException e) {
            return r;
        }
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return Collections.unmodifiableCollection(orderedServices);
    }

    @Override
    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Audit(action = "SAVE_SERVICE", actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService save(final RegisteredService registeredService) {
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);
        this.orderedServices = convertToTreeSet();
        publishEvent(new CasRegisteredServiceSavedEvent(this, r));
        return r;
    }

    /**
     * Load services that are provided by the DAO.
     */
    @Scheduled(initialDelayString = "${cas.serviceRegistry.startDelay:20000}",
               fixedDelayString = "${cas.serviceRegistry.repeatInterval:60000}")
    @Override
    @PostConstruct
    public void load() {
        LOGGER.debug("Loading services from {}", this.serviceRegistryDao);
        this.services = this.serviceRegistryDao.load().stream()
                .collect(Collectors.toConcurrentMap(r -> {
                    LOGGER.debug("Adding registered service {}", r.getServiceId());
                    return r.getId();
                }, r -> r, (r, s) -> s == null ? r : s));
        orderedServices = convertToTreeSet();
        LOGGER.info("Loaded {} services from {}.", this.services.size(), this.serviceRegistryDao);
    }

    @Override
    public RegisteredService findServiceBy(final String serviceId) {
        return findServiceBy(this.serviceFactory.createService(serviceId));
    }

    @Override
    public boolean matchesExistingService(final String service) {
        return matchesExistingService(this.serviceFactory.createService(service));
    }

    @Override
    public int count() {
        return services.size();
    }

    /**
     * Handle services manager refresh event.
     *
     * @param event the event
     */
    @EventListener
    protected void handleRefreshEvent(final CasRegisteredServicesRefreshEvent event) {
        load();
    }

    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }

    /**
     * Stuff services to tree set.
     *
     * @return the tree set
     */
    private TreeSet<RegisteredService> convertToTreeSet() {
        return new TreeSet<>(this.services.values());
    }
}
