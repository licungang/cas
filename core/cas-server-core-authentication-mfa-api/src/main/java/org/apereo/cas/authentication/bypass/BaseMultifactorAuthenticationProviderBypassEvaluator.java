package org.apereo.cas.authentication.bypass;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link BaseMultifactorAuthenticationProviderBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public abstract class BaseMultifactorAuthenticationProviderBypassEvaluator implements MultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = 2372899636154131393L;

    private final String providerId;

    private final String id = this.getClass().getSimpleName();

    /**
     * Evaluate attribute rules for bypass.
     *
     * @param attrName               the attr name
     * @param attrValue              the attr value
     * @param attributes             the attributes
     * @param matchIfNoValueProvided the force match on value
     * @return true a matching attribute name/value is found
     */
    protected static boolean locateMatchingAttributeValue(final String attrName, final String attrValue,
                                                          final Map<String, List<Object>> attributes,
                                                          final boolean matchIfNoValueProvided) {
        LOGGER.debug("Locating matching attribute [{}] with value [{}] amongst the attribute collection [{}]", attrName, attrValue, attributes);
        if (StringUtils.isBlank(attrName)) {
            LOGGER.debug("Failed to match since attribute name is undefined");
            return false;
        }

        val names = locateMatchingAttributeName(attributes, attrName);
        if (names.isEmpty()) {
            return false;
        }

        if (StringUtils.isBlank(attrValue)) {
            LOGGER.debug("No attribute value to match is provided; Match result is set to [{}]", matchIfNoValueProvided);
            return matchIfNoValueProvided;
        }

        val values = names
            .entrySet()
            .stream()
            .filter(e -> {
                val valuesCol = CollectionUtils.toCollection(e.getValue());
                LOGGER.debug("Matching attribute [{}] with values [{}] against [{}]", e.getKey(), valuesCol, attrValue);
                return valuesCol
                    .stream()
                    .anyMatch(v -> RegexUtils.find(attrValue, v.toString()));
            }).collect(Collectors.toSet());

        LOGGER.debug("Matching attribute values remaining are [{}]", values);
        return !values.isEmpty();
    }

    protected static Map<String, List<Object>> locateMatchingAttributeName(final Map<String, List<Object>> attributes,
                                                                           final String attrName) {
        val names = attributes.entrySet()
            .stream()
            .filter(e -> {
                LOGGER.debug("Attempting to match [{}] against [{}]", attrName, e.getKey());
                return RegexUtils.find(attrName, e.getKey());
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        LOGGER.debug("Found [{}] attributes relevant for multifactor authentication bypass", names.size());
        return names;
    }

    @Audit(action = AuditableActions.MULTIFACTOR_AUTHENTICATION_BYPASS,
        actionResolverName = AuditActionResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.MULTIFACTOR_AUTHENTICATION_BYPASS_RESOURCE_RESOLVER)
    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication, final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider, final HttpServletRequest request) {
        return shouldMultifactorAuthenticationProviderExecuteInternal(authentication, registeredService, provider, request);
    }

    @Override
    public boolean isMultifactorAuthenticationBypassed(final Authentication authentication, final String requestedContext) {
        val attributes = authentication.getAttributes();
        if (attributes.containsKey(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA)) {

            val result = CollectionUtils.firstElement(attributes.get(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA));
            val providerRes = CollectionUtils.firstElement(attributes.get(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER));

            if (result.isPresent()) {
                val bypass = (Boolean) result.get();
                if (bypass && providerRes.isPresent()) {
                    val provider = providerRes.get().toString();
                    return StringUtils.equalsIgnoreCase(requestedContext, provider);
                }
            }
        }
        return false;
    }

    @Override
    public void forgetBypass(final Authentication authentication) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.FALSE);
    }

    @Override
    public void rememberBypass(final Authentication authentication,
                               final MultifactorAuthenticationProvider provider) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.TRUE);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, provider.getId());
    }

    @Override
    public Optional<MultifactorAuthenticationProviderBypassEvaluator> belongsToMultifactorAuthenticationProvider(final String providerId) {
        if (getProviderId().equalsIgnoreCase(providerId)) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    /**
     * Should multifactor authentication provider execute internal.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param provider          the provider
     * @param request           the request
     * @return true/false
     */
    protected abstract boolean shouldMultifactorAuthenticationProviderExecuteInternal(Authentication authentication,
                                                                                      RegisteredService registeredService,
                                                                                      MultifactorAuthenticationProvider provider,
                                                                                      HttpServletRequest request);

    /**
     * Resolve principal.
     *
     * @param principal the principal
     * @return the principal
     */
    protected Principal resolvePrincipal(final Principal principal) {
        val resolvers = ApplicationContextProvider.getMultifactorAuthenticationPrincipalResolvers();
        return resolvers
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }

}
