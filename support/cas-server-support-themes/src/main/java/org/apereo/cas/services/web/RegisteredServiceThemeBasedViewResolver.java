package org.apereo.cas.services.web;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.spring4.view.AbstractThymeleafView;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

/**
 * {@link RegisteredServiceThemeBasedViewResolver} is an alternate Spring View Resolver that utilizes a service's
 * associated theme to selectively choose which set of UI views will be used to generate
 * the standard views.
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceThemeBasedViewResolver extends ThymeleafViewResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceThemeBasedViewResolver.class);

    private final ServicesManager servicesManager;
    private final ArgumentExtractor argumentExtractor;
    private final String prefix;
    private final String suffix;
    
    private String defaultThemeName;

    public RegisteredServiceThemeBasedViewResolver(final ServicesManager servicesManager,
                                                   final ArgumentExtractor argumentExtractor,
                                                   final String prefix,
                                                   final String suffix) {
        this.servicesManager = servicesManager;
        this.argumentExtractor = argumentExtractor;
        this.prefix = prefix;
        this.suffix = suffix;
        this.defaultThemeName = AbstractThemeResolver.ORIGINAL_DEFAULT_THEME_NAME;
    }
    
    
    /**
     * Set the name of the default theme.
     * Out-of-the-box value is "theme".
     */
    public void setDefaultThemeName(final String defaultThemeName) {
        this.defaultThemeName = defaultThemeName;
    }

    /**
     * Return the name of the default theme.
     */
    public String getDefaultThemeName() {
        return this.defaultThemeName;
    }
    
    @Override
    protected View loadView(final String viewName, final Locale locale) throws Exception {
        final View view = super.loadView(viewName, locale);

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final WebApplicationService service;

        final HttpServletResponse response;
        final List<ArgumentExtractor> argumentExtractorList = CollectionUtils.wrap(this.argumentExtractor);
        
        if (requestContext != null) {
            response = WebUtils.getHttpServletResponse(requestContext);
            service = WebUtils.getService(argumentExtractorList, requestContext);
        } else {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromRequestAttributes();
            service = WebUtils.getService(argumentExtractorList, request);
            response = WebUtils.getHttpServletResponseFromRequestAttributes();
        }

        if (service == null) {
            applyDefaultThemeToTemplateName(view);
            return view;
        } else {
            applyServiceThemeToTemplateName(view, service, response);
            return view;
        }
    }

    private void applyServiceThemeToTemplateName(final View view, final WebApplicationService service, final HttpServletResponse response) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null) {
            try {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            } catch (final Exception e) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        }

        if (registeredService != null && StringUtils.hasText(registeredService.getTheme()) && view instanceof AbstractThymeleafView) {
            LOGGER.debug("Attempting to locate views for service [{}] with theme [{}]",
                    registeredService.getServiceId(), registeredService.getTheme());

            final AbstractThymeleafView thymeleafView = (AbstractThymeleafView) view;
            final String viewUrl = registeredService.getTheme() + '/' + thymeleafView.getTemplateName();

            final boolean success = applyThemeToTemplateName(thymeleafView, viewUrl);
            if (!success) {
                applyDefaultThemeToTemplateName(thymeleafView);
            }
        }
    }

    private void applyDefaultThemeToTemplateName(final View view) {
        if (StringUtils.hasText(defaultThemeName) && view instanceof AbstractThymeleafView) {
            LOGGER.debug("Attempting to locate views for default theme with name [{}]", defaultThemeName);
            final AbstractThymeleafView thymeleafView = (AbstractThymeleafView) view;
            final String viewUrl = defaultThemeName + '/' + thymeleafView.getTemplateName();
            applyThemeToTemplateName(thymeleafView, viewUrl);
        }
    }

    private boolean applyThemeToTemplateName(final AbstractThymeleafView thymeleafView, final String viewUrl) {
        final String viewLocationUrl = prefix.concat(viewUrl).concat(suffix);
        LOGGER.debug("Attempting to locate view at [{}]", viewLocationUrl);
        final TemplateLocation location = new TemplateLocation(viewLocationUrl);
        if (location.exists(getApplicationContext())) {
            LOGGER.debug("Found view [{}]", viewUrl);
            thymeleafView.setTemplateName(viewUrl);
            return true;
        } else {
            LOGGER.debug("View [{}] does not exist. Falling back to default view at [{}]", viewLocationUrl, thymeleafView.getTemplateName());
            return false;
        }
    }
    
}
