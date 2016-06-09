package org.apereo.cas.support.saml.web.idp.profile.builders;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.client.validation.Assertion;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Conditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link SamlProfileSamlConditionsBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlProfileSamlConditionsBuilder extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder<Conditions> {
    private static final long serialVersionUID = 126393045912318783L;

    @Autowired
    private SamlIdPProperties properties;
            
    @Override
    public Conditions build(final AuthnRequest authnRequest, final HttpServletRequest request, final HttpServletResponse response,
                                  final Assertion assertion, final SamlRegisteredService service,
                                  final SamlRegisteredServiceServiceProviderMetadataFacade adaptor)
            throws SamlException {
        return buildConditions(authnRequest, assertion, service, adaptor);
    }

    private Conditions buildConditions(final AuthnRequest authnRequest, final Assertion assertion,
                                       final SamlRegisteredService service,
                                       final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {

        final ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneOffset.UTC);
        final Conditions conditions = newConditions(currentDateTime,
                currentDateTime.plusSeconds(properties.getResponse().getSkewAllowance()),
                adaptor.getEntityId());
        return conditions;
    }
}
