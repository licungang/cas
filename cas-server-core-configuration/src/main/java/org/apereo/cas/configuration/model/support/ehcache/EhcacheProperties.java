package org.apereo.cas.configuration.model.support.ehcache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link EhcacheProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "ehcache", ignoreUnknownFields = false)
public class EhcacheProperties {
    
    private boolean loaderAsync = true;
    
    private int maxChunkSize = 5000000;
    
    private int maximumBatchSize = 100;

    private int replicationInterval = 10000;
    
    private boolean replicatePuts = true;

    private boolean replicateUpdatesViaCopy = true;
    
    private boolean replicateRemovals = true;

    private boolean replicateUpdates = true;

    private boolean replicatePutsViaCopy = true;
    
    private Resource configLocation = new ClassPathResource("ehcache-replicated.xml");
    
    private boolean shared;
    
    private String cacheManagerName = "ticketRegistryCacheManager";

    private String cacheName = "org.apereo.cas.ticket.TicketCache";

    private int diskExpiryThreadIntervalSeconds;
    
    private boolean diskPersistent;
    
    private boolean eternal;
    
    private int maxElementsInMemory = 10000;

    private int maxElementsOnDisk;
    
    private String memoryStoreEvictionPolicy = "LRU";
    
    private boolean overflowToDisk;
    
    private int cacheTimeToIdle;
    
    private int cacheTimeToLive = Integer.MAX_VALUE;

    public boolean isLoaderAsync() {
        return loaderAsync;
    }

    public void setLoaderAsync(final boolean loaderAsync) {
        this.loaderAsync = loaderAsync;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(final int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public int getMaximumBatchSize() {
        return maximumBatchSize;
    }

    public void setMaximumBatchSize(final int maximumBatchSize) {
        this.maximumBatchSize = maximumBatchSize;
    }

    public int getReplicationInterval() {
        return replicationInterval;
    }

    public void setReplicationInterval(final int replicationInterval) {
        this.replicationInterval = replicationInterval;
    }

    public boolean isReplicatePuts() {
        return replicatePuts;
    }

    public void setReplicatePuts(final boolean replicatePuts) {
        this.replicatePuts = replicatePuts;
    }

    public boolean isReplicateUpdatesViaCopy() {
        return replicateUpdatesViaCopy;
    }

    public void setReplicateUpdatesViaCopy(final boolean replicateUpdatesViaCopy) {
        this.replicateUpdatesViaCopy = replicateUpdatesViaCopy;
    }

    public boolean isReplicateRemovals() {
        return replicateRemovals;
    }

    public void setReplicateRemovals(final boolean replicateRemovals) {
        this.replicateRemovals = replicateRemovals;
    }

    public boolean isReplicateUpdates() {
        return replicateUpdates;
    }

    public void setReplicateUpdates(final boolean replicateUpdates) {
        this.replicateUpdates = replicateUpdates;
    }

    public boolean isReplicatePutsViaCopy() {
        return replicatePutsViaCopy;
    }

    public void setReplicatePutsViaCopy(final boolean replicatePutsViaCopy) {
        this.replicatePutsViaCopy = replicatePutsViaCopy;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(final boolean shared) {
        this.shared = shared;
    }

    public String getCacheManagerName() {
        return cacheManagerName;
    }

    public void setCacheManagerName(final String cacheManagerName) {
        this.cacheManagerName = cacheManagerName;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
    }

    public int getDiskExpiryThreadIntervalSeconds() {
        return diskExpiryThreadIntervalSeconds;
    }

    public void setDiskExpiryThreadIntervalSeconds(final int diskExpiryThreadIntervalSeconds) {
        this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
    }

    public boolean isDiskPersistent() {
        return diskPersistent;
    }

    public void setDiskPersistent(final boolean diskPersistent) {
        this.diskPersistent = diskPersistent;
    }

    public boolean isEternal() {
        return eternal;
    }

    public void setEternal(final boolean eternal) {
        this.eternal = eternal;
    }

    public int getMaxElementsInMemory() {
        return maxElementsInMemory;
    }

    public void setMaxElementsInMemory(final int maxElementsInMemory) {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    public int getMaxElementsOnDisk() {
        return maxElementsOnDisk;
    }

    public void setMaxElementsOnDisk(final int maxElementsOnDisk) {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    public String getMemoryStoreEvictionPolicy() {
        return memoryStoreEvictionPolicy;
    }

    public void setMemoryStoreEvictionPolicy(final String memoryStoreEvictionPolicy) {
        this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy;
    }

    public boolean isOverflowToDisk() {
        return overflowToDisk;
    }

    public void setOverflowToDisk(final boolean overflowToDisk) {
        this.overflowToDisk = overflowToDisk;
    }

    public int getCacheTimeToIdle() {
        return cacheTimeToIdle;
    }

    public void setCacheTimeToIdle(final int cacheTimeToIdle) {
        this.cacheTimeToIdle = cacheTimeToIdle;
    }

    public int getCacheTimeToLive() {
        return cacheTimeToLive;
    }

    public void setCacheTimeToLive(final int cacheTimeToLive) {
        this.cacheTimeToLive = cacheTimeToLive;
    }
}


