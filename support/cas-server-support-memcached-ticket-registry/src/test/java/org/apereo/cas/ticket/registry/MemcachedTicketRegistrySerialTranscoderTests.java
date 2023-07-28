package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link MemcachedTicketRegistrySerialTranscoderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated Since 7.0.0
 */
@EnabledIfListeningOnPort(port = 11211)
@Tag("Memcached")
@TestPropertySource(properties = "cas.ticket.registry.memcached.transcoder=SERIAL")
@Deprecated(since = "7.0.0")
class MemcachedTicketRegistrySerialTranscoderTests extends MemcachedTicketRegistryTests {
}
