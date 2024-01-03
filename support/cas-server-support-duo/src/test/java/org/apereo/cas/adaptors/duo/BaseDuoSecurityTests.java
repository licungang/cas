package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.DuoSecurityAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.DuoSecurityComponentSerializationConfiguration;
import org.apereo.cas.config.DuoSecurityConfiguration;
import org.apereo.cas.config.DuoSecurityMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.DuoSecurityRestConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustWebflowConfiguration;
import org.apereo.cas.config.MultifactorAuthnTrustedDeviceFingerprintConfiguration;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseDuoSecurityTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public abstract class BaseDuoSecurityTests {

    public static RequestContext getMockRequestContext(final ConfigurableApplicationContext applicationContext) throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        context.getHttpServletRequest().setRemoteAddr("185.86.151.11");
        context.getHttpServletRequest().setLocalAddr("195.88.151.11");
        context.getHttpServletRequest().addHeader(HttpRequestUtils.USER_AGENT_HEADER, "MSIE");
        ClientInfoHolder.setClientInfo(ClientInfo.from(context.getHttpServletRequest()));

        val provider = getDuoSecurityMultifactorAuthenticationProvider();
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider);

        val targetResolver = new DefaultTargetStateResolver(provider.getId());
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(provider.getId())), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authentication));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);
        return context;
    }

    public static MultifactorAuthenticationProvider getDuoSecurityMultifactorAuthenticationProvider() {
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.matches(argThat(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER::matches))).thenReturn(true);
        return provider;
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        MultifactorAuthnTrustConfiguration.class,
        MultifactorAuthnTrustedDeviceFingerprintConfiguration.class,
        MultifactorAuthnTrustWebflowConfiguration.class,

        DuoSecurityAuthenticationEventExecutionPlanConfiguration.class,
        DuoSecurityComponentSerializationConfiguration.class,
        DuoSecurityMultifactorProviderBypassConfiguration.class,
        DuoSecurityRestConfiguration.class,
        DuoSecurityConfiguration.class,

        CasCoreNotificationsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasWebflowAutoConfiguration.class,
        CasThemesAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreAuditAutoConfiguration.class,
        CasCoreMultifactorAuthenticationAutoConfiguration.class,
        CasMultifactorAuthenticationWebflowAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCookieAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
