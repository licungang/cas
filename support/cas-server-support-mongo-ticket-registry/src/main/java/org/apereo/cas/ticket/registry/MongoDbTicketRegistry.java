package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.StreamUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Ticket Registry storage backend based on MongoDB.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class MongoDbTicketRegistry extends AbstractTicketRegistry {

    private final TicketCatalog ticketCatalog;

    private final MongoOperations mongoTemplate;

    private final TicketSerializationManager ticketSerializationManager;

    /**
     * Calculate the time at which the ticket is eligible for automated deletion by MongoDb.
     * Makes the assumption that the CAS server date and the Mongo server date are in sync.
     */
    private static Date getExpireAt(final Ticket ticket) {
        val expirationPolicy = ticket.getExpirationPolicy();
        val ttl = expirationPolicy.getTimeToLive(ticket);
        val tti = expirationPolicy.getTimeToIdle();
        val expireAtTti = tti>0 && tti != Long.MAX_VALUE ? System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(tti) : -1;
        val expireAtTtl = ttl>0 && ttl != Long.MAX_VALUE ? ticket.getCreationTime().toInstant().toEpochMilli() + TimeUnit.SECONDS.toMillis(ttl) : -1;
        val maxExpireAt = Math.max(expireAtTti, expireAtTtl);
        if (maxExpireAt == -1) {
            LOGGER.trace("Expiration date for [{}] is undefined for tti value [{}] / ttl value [{}]", ticket.getId(), tti, ttl);
            return null;
        }
        val expireAt = DateTimeUtils.dateOf(Instant.ofEpochMilli(maxExpireAt));
        LOGGER.trace("Expiration date for [{}] : [{}]", ticket.getId(), expireAt);
        return DateTimeUtils.dateOf(Instant.ofEpochMilli(maxExpireAt));
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket.getId());
            val holder = buildTicketAsDocument(ticket);
            val metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
                return;
            }
            LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            LOGGER.trace("Found collection [{}] linked to ticket [{}]", collectionName, metadata);
            this.mongoTemplate.insert(holder, collectionName);
            LOGGER.debug("Added ticket [{}]", ticket.getId());
        } catch (final Exception e) {
            LOGGER.error("Failed adding [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            LOGGER.debug("Locating ticket ticketId [{}]", ticketId);
            val encTicketId = encodeTicketId(ticketId);
            if (encTicketId == null) {
                LOGGER.debug("Ticket id [{}] could not be found", ticketId);
                return null;
            }
            val metadata = this.ticketCatalog.find(ticketId);
            if (metadata == null) {
                LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
                return null;
            }
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(encTicketId));
            val d = this.mongoTemplate.findOne(query, TicketHolder.class, collectionName);
            if (d != null) {
                val decoded = deserializeTicketFromMongoDocument(d);
                val result = decodeTicket(decoded);

                if (predicate.test(result)) {
                    return result;
                }
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}]", ticketId);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public long deleteAll() {
        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).exists(true));
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .filter(StringUtils::isNotBlank)
            .mapToLong(collectionName -> {
                val countTickets = this.mongoTemplate.count(query, collectionName);
                mongoTemplate.remove(query, collectionName);
                return countTickets;
            })
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .map(map -> mongoTemplate.findAll(TicketHolder.class, map))
            .flatMap(List::stream)
            .map(ticket -> decodeTicket(deserializeTicketFromMongoDocument(ticket)))
            .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        LOGGER.debug("Updating ticket [{}]", ticket);
        try {
            val holder = buildTicketAsDocument(ticket);
            val metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
                return null;
            }
            LOGGER.debug("Located ticket definition [{}] in the ticket catalog", metadata);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(holder.getTicketId()));
            val update = Update.update(TicketHolder.FIELD_NAME_JSON, holder.getJson());
            update.set(TicketHolder.FIELD_NAME_EXPIRE_AT, holder.getExpireAt());
            val result = this.mongoTemplate.updateFirst(query, update, collectionName);
            LOGGER.debug("Updated ticket [{}] with result [{}]", ticket, result);
            return result.getMatchedCount() > 0 ? ticket : null;
        } catch (final Exception e) {
            LOGGER.error("Failed updating [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public Stream<Ticket> stream() {
        return ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .map(map -> mongoTemplate.stream(new Query(), TicketHolder.class, map))
            .flatMap(StreamUtils::createStreamFromIterator)
            .map(ticket -> decodeTicket(deserializeTicketFromMongoDocument(ticket)));
    }

    @Override
    public long sessionCount() {
        return countTicketsByTicketType(TicketGrantingTicket.class);
    }

    @Override
    public long countSessionsFor(final String principalId) {
        return getSessionsFor(principalId).count();
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val ticketDefinitions = ticketCatalog.findTicketImplementations(TicketGrantingTicket.class);
        return ticketDefinitions
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .map(map -> {
                val query = isCipherExecutorEnabled()
                    ? new Query(Criteria.where(TicketHolder.FIELD_NAME_PRINCIPAL).is(encodeTicketId(principalId)))
                    : TextQuery.queryText(TextCriteria.forDefaultLanguage().matchingAny(principalId)).sortByScore().with(PageRequest.of(0, 10));
                return mongoTemplate.stream(query, TicketHolder.class, map);
            })
            .flatMap(StreamUtils::createStreamFromIterator)
            .map(ticket -> decodeTicket(deserializeTicketFromMongoDocument(ticket)));
    }

    @Override
    public long serviceTicketCount() {
        return countTicketsByTicketType(ServiceTicket.class);
    }

    @Override
    public long deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.debug("Deleting ticket [{}]", ticketId);
        val metadata = this.ticketCatalog.find(ticketIdToDelete);
        val collectionName = getTicketCollectionInstanceByMetadata(metadata);
        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticketId));
        val res = this.mongoTemplate.remove(query, collectionName);
        LOGGER.debug("Deleted ticket [{}] with result [{}]", ticketIdToDelete, res);
        return res.getDeletedCount();
    }

    protected long countTicketsByTicketType(final Class<? extends Ticket> ticketType) {
        val ticketDefinitions = ticketCatalog.findTicketImplementations(ticketType);
        return ticketDefinitions.stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .mapToLong(map -> mongoTemplate.count(new Query(), map))
            .sum();
    }

    protected TicketHolder buildTicketAsDocument(final Ticket ticket) throws Exception {
        val encTicket = encodeTicket(ticket);
        val json = serializeTicketForMongoDocument(encTicket);
        FunctionUtils.throwIf(StringUtils.isBlank(json),
            () -> new IllegalArgumentException("Ticket " + ticket.getId() + " cannot be serialized to JSON"));
        LOGGER.trace("Serialized ticket into a JSON document as\n [{}]",
            JsonValue.readJSON(json).toString(Stringify.FORMATTED));
        val expireAt = getExpireAt(ticket);
        LOGGER.trace("Calculated expiration date for ticket ttl as [{}]", expireAt);
        val principal = getPrincipalIdFrom(ticket);
        return TicketHolder.builder()
            .expireAt(expireAt)
            .type(encTicket.getClass().getName())
            .ticketId(encTicket.getId())
            .json(json)
            .principal(encodeTicketId(principal))
            .build();
    }

    protected String getTicketCollectionInstanceByMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating collection name [{}] for ticket definition [{}]", mapName, metadata);
        val collection = getTicketCollectionInstance(mapName);
        return Objects.requireNonNull(collection).getNamespace().getCollectionName();
    }

    protected MongoCollection getTicketCollectionInstance(final String mapName) {
        return FunctionUtils.doUnchecked(() -> {
            val inst = this.mongoTemplate.getCollection(mapName);
            LOGGER.debug("Located MongoDb collection instance [{}]", mapName);
            return inst;
        });
    }

    protected String serializeTicketForMongoDocument(final Ticket ticket) {
        return ticketSerializationManager.serializeTicket(ticket);
    }

    protected Ticket deserializeTicketFromMongoDocument(final TicketHolder holder) {
        return ticketSerializationManager.deserializeTicket(holder.getJson(), holder.getType());
    }
}

