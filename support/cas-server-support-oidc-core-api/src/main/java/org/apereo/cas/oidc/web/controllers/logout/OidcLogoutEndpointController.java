package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.EncodingUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link OidcLogoutEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OidcLogoutEndpointController extends BaseOAuth20Controller {
    public OidcLogoutEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request.
     *
     * @param postLogoutRedirectUrl the post logout redirect url
     * @param state                 the state
     * @param idToken               the id token
     * @param request               the request
     * @param response              the response
     * @return the response entity
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.LOGOUT_URL)
    @SneakyThrows
    public View handleRequestInternal(@RequestParam(value = "post_logout_redirect_uri", required = false) final String postLogoutRedirectUrl,
                                      @RequestParam(value = "state", required = false) final String state,
                                      @RequestParam(value = "id_token_hint", required = false) final String idToken,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {

        if (StringUtils.isNotBlank(idToken)) {
            val claims = getOAuthConfigurationContext().getIdTokenSigningAndEncryptionService().decode(idToken, Optional.empty());
            val clientId = claims.getStringClaimValue(OAuth20Constants.CLIENT_ID);

            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
            val service = getOAuthConfigurationContext().getWebApplicationServiceServiceFactory()
                .createService(clientId);

            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .retrievePrincipalAttributesFromReleasePolicy(Boolean.FALSE)
                .build();
            val accessResult = getOAuthConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            val urls = getOAuthConfigurationContext().getSingleLogoutServiceLogoutUrlBuilder()
                .determineLogoutUrl(registeredService, service, Optional.of(request));
            if (StringUtils.isNotBlank(postLogoutRedirectUrl)) {
                val matchResult = urls.stream().anyMatch(url -> url.getUrl().equalsIgnoreCase(postLogoutRedirectUrl));
                if (matchResult) {
                    return getLogoutRedirectView(Optional.ofNullable(StringUtils.trimToNull(state)),
                        Optional.of(postLogoutRedirectUrl), Optional.of(clientId));
                }
            }

            if (urls.isEmpty()) {
                return getLogoutRedirectView(Optional.ofNullable(StringUtils.trimToNull(state)), Optional.empty(), Optional.of(clientId));
            }
            return getLogoutRedirectView(Optional.ofNullable(StringUtils.trimToNull(state)),
                Optional.of(urls.iterator().next().getUrl()), Optional.of(clientId));
        }

        return getLogoutRedirectView(Optional.ofNullable(StringUtils.trimToNull(state)),
            Optional.empty(), Optional.empty());
    }

    /**
     * Gets logout redirect view.
     *
     * @param state       the state
     * @param redirectUrl the redirect url
     * @param clientId    the client id
     * @return the logout redirect view
     */
    protected View getLogoutRedirectView(final Optional<String> state, final Optional<String> redirectUrl,
                                         final Optional<String> clientId) {
        val builder = UriComponentsBuilder.fromHttpUrl(getOAuthConfigurationContext().getCasProperties().getServer().getLogoutUrl());
        redirectUrl.ifPresent(url -> {
            val logout = getOAuthConfigurationContext().getCasProperties().getLogout();
            builder.queryParam(logout.getRedirectParameter(), constructRedirectUrl(url, state, clientId));
        });
        state.ifPresent(st -> builder.queryParam(OAuth20Constants.STATE, st));
        clientId.ifPresent(id -> builder.queryParam(OAuth20Constants.CLIENT_ID, id));

        val logoutUrl = builder.build().toUriString();
        return new RedirectView(logoutUrl);
    }

    /**
     * Constructs the URL to use to redirect to the calling server.
     *
     * @param redirectUrl the actual service's url.
     * @param state       whether we should send.
     * @param clientId    the client id
     * @return the fully constructed redirect url.
     */
    protected String constructRedirectUrl(final String redirectUrl, final Optional<String> state,
                                          final Optional<String> clientId) {
        val builder = UriComponentsBuilder.fromHttpUrl(redirectUrl);
        state.ifPresent(st -> builder.queryParam(OAuth20Constants.STATE, st));
        return EncodingUtils.urlEncode(builder.build().toUriString());
    }
}
