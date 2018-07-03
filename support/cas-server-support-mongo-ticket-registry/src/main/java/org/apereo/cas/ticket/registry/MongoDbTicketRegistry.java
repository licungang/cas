package org.apereo.cas.ticket.registry;

import com.google.common.collect.ImmutableSet;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.ticket.BaseTicketSerializers;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.bson.Document;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Ticket Registry storage backend based on MongoDB.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class MongoDbTicketRegistry extends AbstractTicketRegistry {
    private static final Query SELECT_ALL_NAMES_QUERY = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).regex(".+"));

    private static final ImmutableSet<String> MONGO_INDEX_KEYS = ImmutableSet.of("v", "key", "name", "ns");

    private final TicketCatalog ticketCatalog;
    private final MongoOperations mongoTemplate;
    private final boolean dropCollection;

    public MongoDbTicketRegistry(final TicketCatalog ticketCatalog,
                                 final MongoOperations mongoTemplate,
                                 final boolean dropCollection) {
        this.ticketCatalog = ticketCatalog;
        this.mongoTemplate = mongoTemplate;
        this.dropCollection = dropCollection;

        createTicketCollections();
        LOGGER.info("Configured MongoDb Ticket Registry instance with available collections: [{}]", mongoTemplate.getCollectionNames());
    }

    private MongoCollection createTicketCollection(final TicketDefinition ticket, final MongoDbConnectionFactory factory) {
        final var collectionName = ticket.getProperties().getStorageName();
        LOGGER.debug("Setting up MongoDb Ticket Registry instance [{}]", collectionName);
        factory.createCollection(mongoTemplate, collectionName, this.dropCollection);

        LOGGER.debug("Creating indices on collection [{}] to auto-expire documents...", collectionName);
        final MongoCollection collection = mongoTemplate.getCollection(collectionName);
        final var index = new Index().on("expireAt", Sort.Direction.ASC).expire(ticket.getProperties().getStorageTimeout());
        removeDifferingIndexIfAny(collection, index);
        mongoTemplate.indexOps(TicketHolder.class).ensureIndex(index);
        return collection;
    }

    /**
     * Remove any index with the same indexKey but differing indexOptions in anticipation of recreating it.
     * @param collection The collection to check the indexes of
     * @param index The index to find
     */
    private void removeDifferingIndexIfAny(final MongoCollection collection, final Index index) {
        final ListIndexesIterable<Document> indexes = collection.listIndexes();
        boolean indexExistsWithDifferentOptions = false;
        for (final var existingIndex : indexes) {
            final boolean keyMatches = existingIndex.get("key").equals(index.getIndexKeys());
            final boolean optionsMatch = index.getIndexOptions().entrySet().stream().allMatch(entry -> entry.getValue().equals(existingIndex.get(entry.getKey())));
            final boolean noExtraOptions = existingIndex.keySet().stream().allMatch(key -> MONGO_INDEX_KEYS.contains(key) || index.getIndexOptions().keySet().contains(key));

            indexExistsWithDifferentOptions |= keyMatches && !(optionsMatch && noExtraOptions);
        }
        if (indexExistsWithDifferentOptions) {
            LOGGER.debug("Removing MongoDb index [{}] from [{}] because it appears to already exist in a different form", index.getIndexKeys(), collection.getNamespace());
            collection.dropIndex(index.getIndexKeys());
        }
    }

    private void createTicketCollections() {
        final var definitions = ticketCatalog.findAll();
        final var factory = new MongoDbConnectionFactory();
        definitions.forEach(t -> {
            final var c = createTicketCollection(t, factory);
            LOGGER.debug("Created MongoDb collection configuration for [{}]", c.getNamespace().getFullName());
        });
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        LOGGER.debug("Updating ticket [{}]", ticket);
        try {
            final var holder = buildTicketAsDocument(ticket);
            final var metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
                return null;
            }
            LOGGER.debug("Located ticket definition [{}] in the ticket catalog", metadata);
            final var collectionName = getTicketCollectionInstanceByMetadata(metadata);
            if (StringUtils.isBlank(collectionName)) {
                LOGGER.error("Could not locate collection linked to ticket definition for ticket [{}]", ticket.getId());
                return null;
            }
            final var query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(holder.getTicketId()));
            final var update = Update.update(TicketHolder.FIELD_NAME_JSON, holder.getJson());
            this.mongoTemplate.upsert(query, update, collectionName);
            LOGGER.debug("Updated ticket [{}]", ticket);
        } catch (final Exception e) {
            LOGGER.error("Failed updating [{}]: [{}]", ticket, e);
        }
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket.getId());
            final var holder = buildTicketAsDocument(ticket);
            final var metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
                return;
            }
            LOGGER.debug("Located ticket definition [{}] in the ticket catalog", metadata);
            final var collectionName = getTicketCollectionInstanceByMetadata(metadata);
            if (StringUtils.isBlank(collectionName)) {
                LOGGER.error("Could not locate collection linked to ticket definition for ticket [{}]", ticket.getId());
                return;
            }
            LOGGER.debug("Found collection [{}] linked to ticket [{}]", collectionName, metadata);
            this.mongoTemplate.insert(holder, collectionName);
            LOGGER.debug("Added ticket [{}]", ticket.getId());
        } catch (final Exception e) {
            LOGGER.error("Failed adding [{}]: [{}]", ticket, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            LOGGER.debug("Locating ticket ticketId [{}]", ticketId);
            final var encTicketId = encodeTicketId(ticketId);
            if (encTicketId == null) {
                LOGGER.debug("Ticket ticketId [{}] could not be found", ticketId);
                return null;
            }
            final var metadata = this.ticketCatalog.find(ticketId);
            if (metadata == null) {
                LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
                return null;
            }
            final var collectionName = getTicketCollectionInstanceByMetadata(metadata);
            final var query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(encTicketId));
            final var d = this.mongoTemplate.findOne(query, TicketHolder.class, collectionName);
            if (d != null) {
                final var decoded = deserializeTicketFromMongoDocument(d);
                final var result = decodeTicket(decoded);

                if (result != null && result.isExpired()) {
                    LOGGER.debug("Ticket [{}] has expired and is now removed from the collection", result.getId());
                    deleteSingleTicket(result.getId());
                    return null;
                }
                return result;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}]: [{}]", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .map(map -> mongoTemplate.findAll(TicketHolder.class, map))
            .flatMap(List::stream)
            .map(ticket -> decodeTicket(deserializeTicketFromMongoDocument(ticket)))
            .collect(Collectors.toSet());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        final var ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.debug("Deleting ticket [{}]", ticketId);
        try {
            final var metadata = this.ticketCatalog.find(ticketIdToDelete);
            final var collectionName = getTicketCollectionInstanceByMetadata(metadata);
            final var query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticketId));
            final var res = this.mongoTemplate.remove(query, collectionName);
            LOGGER.debug("Deleted ticket [{}] with result [{}]", ticketIdToDelete, res);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Failed deleting [{}]: [{}]", ticketId, e);
        }
        return false;
    }

    @Override
    public long deleteAll() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .filter(StringUtils::isNotBlank)
            .mapToLong(collectionName -> {
                final var countTickets = this.mongoTemplate.count(SELECT_ALL_NAMES_QUERY, collectionName);
                mongoTemplate.remove(SELECT_ALL_NAMES_QUERY, collectionName);
                return countTickets;
            })
            .sum();
    }

    /**
     * Calculate the time at which the ticket is eligible for automated deletion by MongoDb.
     * Makes the assumption that the CAS server date and the Mongo server date are in sync.
     */
    private static Date getExpireAt(final Ticket ticket) {
        final long ttl = ticket.getExpirationPolicy().getTimeToLive();

        // expiration policy can specify not to delete automatically
        if (ttl < 1) {
            return null;
        }

        return new Date(System.currentTimeMillis() + (ttl * 1000));
    }

    private static String serializeTicketForMongoDocument(final Ticket ticket) {
        try {
            return BaseTicketSerializers.serializeTicket(ticket);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static Ticket deserializeTicketFromMongoDocument(final TicketHolder holder) {
        return BaseTicketSerializers.deserializeTicket(holder.getJson(), holder.getType());
    }

    private TicketHolder buildTicketAsDocument(final Ticket ticket) {
        final var encTicket = encodeTicket(ticket);
        final var json = serializeTicketForMongoDocument(encTicket);
        if (StringUtils.isNotBlank(json)) {
            LOGGER.trace("Serialized ticket into a JSON document as \n [{}]", JsonValue.readJSON(json).toString(Stringify.FORMATTED));
            final var expireAt = getExpireAt(ticket);
            return new TicketHolder(json, encTicket.getId(), encTicket.getClass().getName(), expireAt);
        }
        throw new IllegalArgumentException("Ticket " + ticket.getId() + " cannot be serialized to JSON");
    }

    private String getTicketCollectionInstanceByMetadata(final TicketDefinition metadata) {
        final var mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating collection name [{}] for ticket definition [{}]", mapName, metadata);
        final var c = getTicketCollectionInstance(mapName);
        if (c != null) {
            return c.getNamespace().getCollectionName();
        }
        throw new IllegalArgumentException("Could not locate MongoDb collection " + mapName);
    }

    private MongoCollection getTicketCollectionInstance(final String mapName) {
        try {
            final MongoCollection inst = this.mongoTemplate.getCollection(mapName);
            LOGGER.debug("Located MongoDb collection instance [{}]", mapName);
            return inst;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}

