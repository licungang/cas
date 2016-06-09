package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AllAuthenticationPolicy;
import org.apereo.cas.authentication.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.FileTrustStoreSslSocketFactory;
import org.apereo.cas.authentication.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.SuccessfulHandlerMetaDataPopulator;
import org.apereo.cas.authentication.handler.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.authentication.handler.DefaultPasswordEncoder;
import org.apereo.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PlainTextPasswordEncoder;
import org.apereo.cas.authentication.handler.PrefixSuffixPrincipalNameTransformer;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandler;
import org.apereo.cas.authentication.principal.BasicPrincipalResolver;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RememberMeAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationExceptionsProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.HttpClientProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.generic.AcceptAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jaas.JaasAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.MfaProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.AuthenticationExceptionHandler;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationConfiguration")
@EnableConfigurationProperties(
        {HttpClientProperties.class,
                PasswordPolicyProperties.class,
                PersonDirPrincipalResolverProperties.class,
                AuthenticationPolicyProperties.class})
public class CasCoreAuthenticationConfiguration {

    @Autowired
    private JaasAuthenticationProperties jaasAuthenticationProperties;

    @Autowired
    private PrincipalTransformationProperties principalTransformationProperties;

    @Autowired
    private MfaProperties mfaProperties;

    @Autowired
    private HttpClientProperties trustStoreProperties;

    @Autowired
    private PasswordPolicyProperties passwordPolicyProperties;

    @Autowired
    private PersonDirPrincipalResolverProperties principalResolverProperties;

    @Autowired
    private AuthenticationPolicyProperties authenticationPolicyProperties;

    @Autowired
    private PasswordEncoderProperties passwordEncoderProperties;

    @Autowired
    private AcceptAuthenticationProperties acceptAuthenticationProperties;

    @Autowired
    private AuthenticationExceptionsProperties exceptionsProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired(required = false)
    @Qualifier("delegateTransformer")
    private PrincipalNameTransformer delegate;


    @Bean
    public AuthenticationExceptionHandler authenticationExceptionHandler() {
        final AuthenticationExceptionHandler h = new AuthenticationExceptionHandler();
        h.setErrors(exceptionsProperties.getExceptions());
        return h;
    }

    @RefreshScope
    @Bean
    public AuthenticationPolicy requiredHandlerAuthenticationPolicy() {
        final RequiredHandlerAuthenticationPolicy bean =
                new RequiredHandlerAuthenticationPolicy(this.authenticationPolicyProperties.getReq().getHandlerName());
        bean.setTryAll(this.authenticationPolicyProperties.getReq().isTryAll());
        return bean;
    }

    @Bean
    public AuthenticationPolicy anyAuthenticationPolicy() {
        final AnyAuthenticationPolicy bean = new AnyAuthenticationPolicy();
        bean.setTryAll(this.authenticationPolicyProperties.getAny().isTryAll());
        return bean;
    }

    @Bean
    public ContextualAuthenticationPolicyFactory acceptAnyAuthenticationPolicyFactory() {
        return new AcceptAnyAuthenticationPolicyFactory();
    }

    @Bean
    public AuthenticationHandler acceptUsersAuthenticationHandler() {
        final Pattern pattern = Pattern.compile("::");
        final AcceptUsersAuthenticationHandler h = new AcceptUsersAuthenticationHandler();
        if (StringUtils.isNotBlank(acceptAuthenticationProperties.getUsers())) {
            final Set<String> usersPasswords =
                    org.springframework.util.StringUtils.commaDelimitedListToSet(acceptAuthenticationProperties.getUsers());
            final Map<String, String> parsedUsers = new HashMap<>();
            usersPasswords.stream().forEach(usersPassword -> {
                final String[] splitArray = pattern.split(usersPassword);
                parsedUsers.put(splitArray[0], splitArray[1]);
            });
            h.setUsers(parsedUsers);
        }
        return h;
    }

    @Bean
    public AuthenticationPolicy allAuthenticationPolicy() {
        return new AllAuthenticationPolicy();
    }

    @RefreshScope
    @Bean
    public AuthenticationContextValidator authenticationContextValidator() {
        final AuthenticationContextValidator val = new AuthenticationContextValidator();
        val.setAuthenticationContextAttribute(mfaProperties.getAuthenticationContextAttribute());
        val.setServicesManager(this.servicesManager);
        val.setGlobalFailureMode(mfaProperties.getGlobalFailureMode());
        return val;
    }

    @Bean
    public AuthenticationMetaDataPopulator cacheCredentialsMetaDataPopulator() {
        return new CacheCredentialsMetaDataPopulator();
    }

    @Bean
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport() {
        return new DefaultAuthenticationSystemSupport();
    }

    @Bean
    public AuthenticationTransactionManager defaultAuthenticationTransactionManager() {
        return new DefaultAuthenticationTransactionManager();
    }

    @Bean
    public PrincipalElectionStrategy defaultPrincipalElectionStrategy() {
        return new DefaultPrincipalElectionStrategy();
    }

    @RefreshScope
    @Bean
    public SSLConnectionSocketFactory trustStoreSslSocketFactory() {
        return new FileTrustStoreSslSocketFactory(this.trustStoreProperties.getTruststore().getFile(),
                this.trustStoreProperties.getTruststore().getPsw());
    }

    @Bean
    public AuthenticationPolicy notPreventedAuthenticationPolicy() {
        return new NotPreventedAuthenticationPolicy();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new PolicyBasedAuthenticationManager();
    }

    @Bean
    public RegisteredServiceAuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver() {
        return new RegisteredServiceAuthenticationHandlerResolver();
    }

    @Bean
    public ContextualAuthenticationPolicyFactory requiredHandlerAuthenticationPolicyFactory() {
        return new RequiredHandlerAuthenticationPolicyFactory();
    }

    @Bean
    public AuthenticationMetaDataPopulator successfulHandlerMetaDataPopulator() {
        return new SuccessfulHandlerMetaDataPopulator();
    }

    @Bean
    public PasswordPolicyConfiguration defaultPasswordPolicyConfiguration() {
        return new PasswordPolicyConfiguration(this.passwordPolicyProperties);
    }

    @Bean
    public AuthenticationMetaDataPopulator rememberMeAuthenticationMetaDataPopulator() {
        return new RememberMeAuthenticationMetaDataPopulator();
    }

    @RefreshScope
    @Bean
    @Autowired
    public PrincipalResolver personDirectoryPrincipalResolver(@Qualifier("attributeRepository")
                                                              final IPersonAttributeDao attributeRepository,
                                                              @Qualifier("principalFactory")
                                                              final PrincipalFactory principalFactory) {
        final PersonDirectoryPrincipalResolver bean = new PersonDirectoryPrincipalResolver();
        bean.setAttributeRepository(attributeRepository);
        bean.setPrincipalFactory(principalFactory);
        bean.setPrincipalAttributeName(this.principalResolverProperties.getPrincipalAttribute());
        bean.setReturnNullIfNoAttributes(this.principalResolverProperties.isReturnNull());
        return bean;
    }

    @Bean
    public PrincipalFactory defaultPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public PrincipalResolver proxyPrincipalResolver() {
        return new BasicPrincipalResolver();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler jaasAuthenticationHandler() {
        final JaasAuthenticationHandler h = new JaasAuthenticationHandler();

        h.setKerberosKdcSystemProperty(jaasAuthenticationProperties.getKerberosKdcSystemProperty());
        h.setKerberosRealmSystemProperty(jaasAuthenticationProperties.getKerberosRealmSystemProperty());
        h.setRealm(jaasAuthenticationProperties.getRealm());

        return h;
    }

    @Bean
    public AuthenticationHandler proxyAuthenticationHandler() {
        return new HttpBasedServiceCredentialsAuthenticationHandler();
    }

    @Bean
    public PrincipalNameTransformer prefixSuffixPrincipalNameTransformer() {
        final PrefixSuffixPrincipalNameTransformer p = new PrefixSuffixPrincipalNameTransformer();

        p.setPrefix(principalTransformationProperties.getPrefix());
        p.setSuffix(principalTransformationProperties.getSuffix());

        return p;
    }

    @Bean
    public PasswordEncoder plainTextPasswordEncoder() {
        return new PlainTextPasswordEncoder();
    }

    @Bean
    public PrincipalNameTransformer noOpPrincipalNameTransformer() {
        return new NoOpPrincipalNameTransformer();
    }

    @RefreshScope
    @Bean
    public DefaultPasswordEncoder defaultPasswordEncoder() {
        final DefaultPasswordEncoder e = new DefaultPasswordEncoder();
        e.setCharacterEncoding(passwordEncoderProperties.getCharacterEncoding());
        e.setEncodingAlgorithm(passwordEncoderProperties.getEncodingAlgorithm());
        return e;
    }

    @Bean
    @RefreshScope
    public PrincipalNameTransformer convertCasePrincipalNameTransformer() {
        final ConvertCasePrincipalNameTransformer t =
                new ConvertCasePrincipalNameTransformer(this.delegate);
        t.setToUpperCase(principalTransformationProperties.isUppercase());
        return t;
    }

}
