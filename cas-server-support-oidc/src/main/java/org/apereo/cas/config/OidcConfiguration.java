package org.apereo.cas.config;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.support.oauth.ticket.code.OAuthCodeFactory;
import org.apereo.cas.support.oauth.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.support.oauth.validator.OAuthValidator;
import org.apereo.cas.support.oauth.web.AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.web.OidcAccessTokenController;
import org.apereo.cas.web.OidcAccessTokenResponseGenerator;
import org.apereo.cas.web.OidcAuthorizeController;
import org.apereo.cas.web.OidcConsentApprovalViewResolver;
import org.apereo.cas.web.OidcProfileController;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.springframework.web.RequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("oidcConfiguration")
public class OidcConfiguration extends WebMvcConfigurerAdapter {

    @Resource
    private OidcProperties properties;

    @Autowired
    @Qualifier("oauthInterceptor")
    private HandlerInterceptor oauthInterceptor;

    @Autowired
    @Qualifier("oauthSecConfig")
    private Config oauthSecConfig;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory defaultAccessTokenFactory;

    @Autowired
    @Qualifier("defaultRefreshTokenFactory")
    private RefreshTokenFactory defaultRefreshTokenFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("oAuthValidator")
    private OAuthValidator oAuthValidator;

    @Autowired
    @Qualifier("defaultOAuthCodeFactory")
    private OAuthCodeFactory defaultOAuthCodeFactory;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(oidcInterceptor())
                .addPathPatterns('/' + OidcConstants.BASE_OIDC_URL.concat("/").concat("*"));
    }

    /**
     * Consent approval view resolver.
     *
     * @return the consent approval view resolver
     */
    @Bean
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        final OidcConsentApprovalViewResolver c = new OidcConsentApprovalViewResolver();
        c.setOidcAuthzRequestSupport(oidcAuthorizationRequestSupport());
        return c;
    }

    /**
     * Callback authorize view resolver.
     *
     * @return the oauth 20 callback authorize view resolver
     */
    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OAuth20CallbackAuthorizeViewResolver() {
            @Override
            public ModelAndView resolve(final J2EContext ctx, final ProfileManager manager, final String url) {
                final Set<String> prompts = oidcAuthorizationRequestSupport().getOidcPromptFromAuthorizationRequest(url);
                if (prompts.contains(OidcConstants.PROMPT_NONE)) {
                    if (manager.get(true) != null) {
                        return new ModelAndView(url);
                    }
                    final Map<String, String> model = new HashMap<>();
                    model.put(OAuthConstants.ERROR, OidcConstants.LOGIN_REQUIRED);
                    return new ModelAndView(new MappingJackson2JsonView(), model);
                }
                return new ModelAndView(new RedirectView(url));
            }
        };
    }

    /**
     * Oidc interceptor handler interceptor.
     *
     * @return the handler interceptor
     */
    @Bean
    public HandlerInterceptor oidcInterceptor() {
        return this.oauthInterceptor;
    }

    /**
     * Oauth cas client redirect action builder.
     *
     * @return the o auth cas client redirect action builder
     */
    @Bean(autowire = Autowire.BY_NAME)
    public OAuthCasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        final OidcCasClientRedirectActionBuilder builder = new OidcCasClientRedirectActionBuilder();
        builder.setOidcAuthorizationRequestSupport(oidcAuthorizationRequestSupport());
        return builder;
    }

    /**
     * Requires authentication authorize interceptor.
     *
     * @return the requires authentication interceptor
     */
    @Bean
    public RequiresAuthenticationInterceptor requiresAuthenticationAuthorizeInterceptor() {
        final String name = oauthSecConfig.getClients().findClient(CasClient.class).getName();
        return new RequiresAuthenticationInterceptor(oauthSecConfig, name) {

            @Override
            public boolean preHandle(final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final Object handler) throws Exception {
                final J2EContext ctx = new J2EContext(request, response);
                final ProfileManager manager = new ProfileManager(ctx);

                boolean clearCreds = false;
                final Optional<UserProfile> auth = oidcAuthorizationRequestSupport().isAuthenticationProfileAvailable(ctx);

                if (auth.isPresent()) {
                    final Optional<Long> maxAge = oidcAuthorizationRequestSupport().getOidcMaxAgeFromAuthorizationRequest(ctx);
                    if (maxAge.isPresent()) {
                        clearCreds = oidcAuthorizationRequestSupport().isCasAuthenticationOldForMaxAgeAuthorizationRequest(ctx, auth.get());
                    }
                }

                final Set<String> prompts = oidcAuthorizationRequestSupport().getOidcPromptFromAuthorizationRequest(ctx);
                if (!clearCreds) {
                    clearCreds = prompts.contains(OidcConstants.PROMPT_LOGIN);
                }

                if (clearCreds) {
                    clearCreds = !prompts.contains(OidcConstants.PROMPT_NONE);
                }

                if (clearCreds) {
                    manager.remove(true);
                }
                return super.preHandle(request, response, handler);
            }
        };
    }

    @Bean
    public OAuthCasClientRedirectActionBuilder oidcCasClientRedirectActionBuilder() {
        return new OidcCasClientRedirectActionBuilder();
    }

    @Bean
    @RefreshScope
    public AccessTokenResponseGenerator oidcAccessTokenResponseGenerator() {
        final OidcAccessTokenResponseGenerator gen = new OidcAccessTokenResponseGenerator();

        gen.setIssuer(properties.getIssuer());
        gen.setJwksFile(properties.getJwksFile());
        gen.setSkew(properties.getSkew());

        return gen;
    }

    @Bean
    public OidcAuthorizationRequestSupport oidcAuthorizationRequestSupport() {
        final OidcAuthorizationRequestSupport s = new OidcAuthorizationRequestSupport();
        s.setTicketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator);
        s.setTicketRegistrySupport(ticketRegistrySupport);
        return s;
    }

    @Bean
    public PrincipalFactory oidcPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public OidcAccessTokenController oidcAccessTokenController() {
        final OidcAccessTokenController c = new OidcAccessTokenController();
        c.setAccessTokenResponseGenerator(oidcAccessTokenResponseGenerator());
        c.setAccessTokenFactory(defaultAccessTokenFactory);
        c.setPrincipalFactory(oidcPrincipalFactory());
        c.setRefreshTokenFactory(defaultRefreshTokenFactory);
        c.setServicesManager(servicesManager);
        c.setTicketRegistry(ticketRegistry);
        c.setValidator(oAuthValidator);
        return c;
    }

    @Bean
    public OidcProfileController oidcProfileController() {
        final OidcProfileController c = new OidcProfileController();
        c.setAccessTokenFactory(defaultAccessTokenFactory);
        c.setServicesManager(servicesManager);
        c.setTicketRegistry(ticketRegistry);
        c.setValidator(oAuthValidator);
        c.setPrincipalFactory(oidcPrincipalFactory());
        return c;
    }

    @Bean
    public OidcAuthorizeController oidcAuthorizeController() {
        final OidcAuthorizeController c = new OidcAuthorizeController();
        c.setAccessTokenFactory(defaultAccessTokenFactory);
        c.setServicesManager(servicesManager);
        c.setTicketRegistry(ticketRegistry);
        c.setValidator(oAuthValidator);
        c.setPrincipalFactory(oidcPrincipalFactory());
        c.setConsentApprovalViewResolver(consentApprovalViewResolver());
        c.setoAuthCodeFactory(defaultOAuthCodeFactory);
        return c;
    }
}













