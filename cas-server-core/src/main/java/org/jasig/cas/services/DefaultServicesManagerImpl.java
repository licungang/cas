/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.github.inspektr.audit.annotation.Audit;
import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

/**
 * Default implementation of the {@link ServicesManager} interface. If there are
 * no services registered with the server, it considers the ServicecsManager
 * disabled and will not prevent any service from using CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class DefaultServicesManagerImpl implements ReloadableServicesManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Instance of ServiceRegistryDao. */
    @NotNull
    private ServiceRegistryDao serviceRegistryDao;

    /** Map to store all services. */
    private ConcurrentHashMap<Long, RegisteredService> services = new ConcurrentHashMap<Long, RegisteredService>();

    /** Default service to return if none have been registered. */
    private RegisteredService disabledRegisteredService;



    /**
     * The first matching service of these will be individually just-in-time registered when findServiceBy(Service)
     * would otherwise return null and one of these matches.
     *
     * The intent of this feature is to prevent a failure of access to CAS-reliant services involved in managing
     * CAS behaviors towards CAS-reliant services; i.e., the Services Management web application component of the
     * CAS server.  By including a matching registration for that service in this List, CAS administrators are never
     * prevented from CAS authenticating to that Services Management service for lack of that service having been
     * already registered.
     *
     * When null, no services will be automatically registered.  Defaults to null.
     */
    private List<RegisteredService> automaticallyRegisteredServices = null;
    
    public DefaultServicesManagerImpl(
        final ServiceRegistryDao serviceRegistryDao) {
        this(serviceRegistryDao, new ArrayList<String>());
    }
    
    /**
     * Constructs an instance of the {@link DefaultServicesManagerImpl} where the default RegisteredService
     * can include a set of default attributes to use if no services are defined in the registry.
     * 
     * @param serviceRegistryDao the Service Registry Dao.
     * @param defaultAttributes the list of default attributes to use.
     */
    public DefaultServicesManagerImpl(final ServiceRegistryDao serviceRegistryDao, final List<String> defaultAttributes) {
        this.serviceRegistryDao = serviceRegistryDao;
        this.disabledRegisteredService = constructDefaultRegisteredService(defaultAttributes);
        
        load();
    }

    @Transactional(readOnly = false)
    @Audit(action = "DELETE_SERVICE", actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER", resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    public synchronized RegisteredService delete(final long id) {
        final RegisteredService r = findServiceBy(id);
        if (r == null) {
            return null;
        }
        
        this.serviceRegistryDao.delete(r);
        this.services.remove(id);
        
        return r;
    }

    /**
     * Note, if the repository is empty, this implementation will return a default service to grant all access.
     * <p>
     * This preserves default CAS behavior.
     */
    public RegisteredService findServiceBy(final Service service) {

        // order is important; match the first registered service ordered by the order property
        final Collection<RegisteredService> c = convertToTreeSet();
        
        if (c.isEmpty()) {
            // no services are registered.  Default to allowing all services to use CAS.
            return this.disabledRegisteredService;
        }

        for (final RegisteredService r : c) {
            if (r.matches(service)) {
                // return the first matching service
                return r;
            }
        }

        // no registration matched.

        // just-in-time register the first automatic service that matches, if any
        if (this.automaticallyRegisteredServices != null) {
            for (RegisteredService notYetRegistered : this.automaticallyRegisteredServices) {
                if (notYetRegistered.matches(service)) {

                    log.warn("Automatically registering [{}].", notYetRegistered);

										// return result from find, rather than notYetRegistered, so that it includes an id
                    return save(notYetRegistered);
                }
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("No existing registration and no matching automatic registration for service [{}].", service);
        }
        return null;
    }

    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);
        
        try {
            return r == null ? null : (RegisteredService) r.clone();
        } catch (final CloneNotSupportedException e) {
            return r;
        }
    }
    
    protected TreeSet<RegisteredService> convertToTreeSet() {
        return new TreeSet<RegisteredService>(this.services.values());
    }

    public Collection<RegisteredService> getAllServices() {
        return Collections.unmodifiableCollection(this.services.values());
    }

    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Transactional(readOnly = false)
    @Audit(action = "SAVE_SERVICE", actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER", resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    public synchronized RegisteredService save(final RegisteredService registeredService) {
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);
        return r;
    }
    
    public void reload() {
        log.info("Reloading registered services.");
        load();
    }
    
    private void load() {
        final ConcurrentHashMap<Long, RegisteredService> localServices = new ConcurrentHashMap<Long, RegisteredService>();
                
        for (final RegisteredService r : this.serviceRegistryDao.load()) {
            log.debug("Adding registered service [{}].", r.getServiceId());
            localServices.put(r.getId(), r);
        }
        
        this.services = localServices;
        log.info(String.format("Loaded %s services.", this.services.size()));
    }
    
    private RegisteredService constructDefaultRegisteredService(final List<String> attributes) {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setAllowedToProxy(true);
        r.setAnonymousAccess(false);
        r.setEnabled(true);
        r.setSsoEnabled(true);
        r.setAllowedAttributes(attributes);
        
        if (attributes == null || attributes.isEmpty()) {
            r.setIgnoreAttributes(true);
        }

        return r;
    }

     public List<RegisteredService> getAutomaticallyRegisteredServices() {
        return automaticallyRegisteredServices;
    }

    public void setAutomaticallyRegisteredServices(List<RegisteredService> automaticallyRegisteredServices) {
        this.automaticallyRegisteredServices = automaticallyRegisteredServices;
    }
}
