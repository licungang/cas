package org.jasig.cas.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Sets;
import com.nimbusds.jwt.PlainJWT;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.OidcConstants;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.OAuthAccessTokenResponseGenerator;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshToken;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.Key;
import java.util.UUID;

/**
 * This is {@link OidcAccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("oidcAccessTokenResponseGenerator")
public class OidcAccessTokenResponseGenerator extends OAuthAccessTokenResponseGenerator {
    
    @Value("${cas.oidc.issuer:http://localhost:8080/cas/oidc}")
    private String issuer;

    @Value("${cas.oidc.skew:5}")
    private int skew;

    @Value("${cas.oidc.jwks:}")
    private Resource jwksFile;

    @Override
    protected void generateJsonInternal(final JsonGenerator jsonGenerator,
                                        final AccessToken accessTokenId,
                                        final RefreshToken refreshTokenId,
                                        final long timeout,
                                        final Service service,
                                        final OAuthRegisteredService registeredService) throws Exception {

        super.generateJsonInternal(jsonGenerator, accessTokenId, refreshTokenId, timeout, service, registeredService);
        
        final String jsonJwks = IOUtils.toString(jwksFile.getInputStream(), "UTF-8");
        final JsonWebKeySet jwks = new JsonWebKeySet(jsonJwks);

        final JwtClaims claims = new JwtClaims();
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(this.issuer);
        claims.setAudience(service.getId());
        claims.setExpirationTimeMinutesInTheFuture(timeout);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(this.skew);
        claims.setSubject(accessTokenId.getAuthentication().getPrincipal().getId());

        final Principal principal = accessTokenId.getAuthentication().getPrincipal();
        final Sets.SetView<String> setView = Sets.intersection(OidcConstants.CLAIMS, principal.getAttributes().keySet());

        setView.immutableCopy().stream().forEach(k -> {
            claims.setClaim(k, principal.getAttributes().get(k));
        });
        
        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
        
        if (!jwks.getJsonWebKeys().isEmpty()) {
            final RsaJsonWebKey jsonWebKey = (RsaJsonWebKey) jwks.getJsonWebKeys().get(0);
            jws.setKey(jsonWebKey.getPrivateKey());
            
            if (StringUtils.isBlank(jsonWebKey.getKeyId())) {
                jws.setKeyIdHeaderValue(UUID.randomUUID().toString());
            } else {
                jws.setKeyIdHeaderValue(jsonWebKey.getKeyId());
            }
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        }
        final String idToken = jws.getCompactSerialization();
        jsonGenerator.writeStringField(OidcConstants.ID_TOKEN, idToken);
    }
}

