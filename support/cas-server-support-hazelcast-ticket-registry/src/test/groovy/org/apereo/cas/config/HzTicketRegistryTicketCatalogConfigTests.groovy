package org.apereo.cas.config

import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.ticket.TicketCatalogConfigurer

/**
 * @author Dmitriy Kopylenko
 */
class HzTicketRegistryTicketCatalogConfigTests extends AbstractTicketRegistryTicketCatalogConfigTests {

    @Override
    TicketCatalogConfigurer ticketCatalogConfigurerUnderTest() {
        new HazelcastTicketRegistryTicketCatalogConfiguration(casProperties: new CasConfigurationProperties())
    }

    @Override
    def TGT_storageNameForConcreteTicketRegistry() {
        'ticketGrantingTicketsCache'
    }

    @Override
    def ST_storageNameForConcreteTicketRegistry() {
        'serviceTicketsCache'
    }

    @Override
    def PGT_storageNameForConcreteTicketRegistry() {
        'proxyGrantingTicketsCache'
    }

    @Override
    def PT_storageNameForConcreteTicketRegistry() {
        'proxyTicketsCache'
    }
}
