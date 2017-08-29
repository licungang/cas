package org.apereo.cas.consent;

import org.apereo.cas.configuration.model.support.consent.ConsentProperties.Ldap;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RandomUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link LdapConsentRepository}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
public class LdapConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 1L;
    
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapConsentRepository.class);
    
    private final ConnectionFactory connectionFactory;
    private final Ldap ldap;
    private final String searchFilter;
    
    public LdapConsentRepository(final ConnectionFactory connectionFactory, final Ldap ldap) {
        this.connectionFactory = connectionFactory;
        this.ldap = ldap;
        this.searchFilter = '(' + this.ldap.getUserFilter() + ')';
    }
    
    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        final LdapEntry entry = readConsentEntry(authentication.getPrincipal().getId());
        if (entry != null) {
            final LdapAttribute consentDecisions = entry.getAttribute(this.ldap.getConsentAttributeName());
            if (consentDecisions != null) {
                return consentDecisions.getStringValues()
                        .stream()
                        .map(s -> mapFromJson(s, ConsentDecision.class))
                        .filter(d -> d.getService().equals(service.getId()))
                        .findFirst()
                        .orElse(null);
            }
        }
        return null;
    }
    
    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        final LdapEntry entry = readConsentEntry(decision.getPrincipal());
        if (entry != null) {
            final Set<String> newConsent = mergeDecision(entry.getAttribute(this.ldap.getConsentAttributeName()), decision);
            final Map<String, Set<String>> attrMap = new HashMap<>();
            attrMap.put(this.ldap.getConsentAttributeName(), newConsent);
            return LdapUtils.executeModifyOperation(entry.getDn(), this.connectionFactory, attrMap);
        }
        return false;
    }

    private Set<String> mergeDecision(final LdapAttribute ldapConsent, final ConsentDecision decision) {
        final Set<String> result = new HashSet<>();
        if (ldapConsent != null && ldapConsent.size() != 0) {
            ldapConsent.getStringValues()
                .stream()
                .map(s -> mapFromJson(s, ConsentDecision.class))
                .filter(d -> d.getId() != decision.getId())
                .map(d -> mapToJson(d, String.class))
                .forEach(s -> result.add(s));
        }
        if (decision.getId() < 0) {
            decision.setId(Math.abs(RandomUtils.getInstanceNative().nextInt()));
        }
        result.add(mapToJson(decision, String.class));
        return result;
    }
        
    private LdapEntry readConsentEntry(final String principal) {
        final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(this.searchFilter, Arrays.asList(principal));
        final String[] attributes = {this.ldap.getConsentAttributeName()};
        final Response<SearchResult> response;
        try {
            response = LdapUtils
                    .executeSearchOperation(this.connectionFactory, this.ldap.getBaseDn(), filter, null, attributes);
            if (LdapUtils.containsResultEntry(response)) {
                return response.getResult().getEntry();
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }
    
    private static <T> ConsentDecision mapFromJson(final String s, final Class<ConsentDecision> c) {
        try {
            return MAPPER.readValue(s, c);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
        
    private static <T> String mapToJson(final ConsentDecision s, final Class<String> c) {
        try {
            return MAPPER.writeValueAsString(s);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);            
        }
        return null;
    }
}
