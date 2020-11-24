package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link DuoSecurityMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-duo")
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(of = {"duoApiHost", "duoIntegrationKey", "duoSecretKey", "duoApplicationKey"}, callSuper = true)
@JsonFilter("DuoSecurityMultifactorProperties")
public class DuoSecurityMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-duo";

    private static final long serialVersionUID = -4655375354167880807L;

    /**
     * Duo integration key.
     */
    @RequiredProperty
    private String duoIntegrationKey;

    /**
     * Duo secret key.
     */
    @RequiredProperty
    private String duoSecretKey;

    /**
     * The duoApplicationKey is a string, at least 40 characters long,
     * that you generate and keep secret from Duo.
     * You can generate a random string in Python with:
     * &lt;pre&gt;
     * import os, hashlib
     * print hashlib.sha1(os.urandom(32)).hexdigest()
     * &lt;/pre&gt;
     */
    @RequiredProperty
    private String duoApplicationKey;

    /**
     * Duo API host and url.
     */
    @RequiredProperty
    private String duoApiHost;

    /**
     * Link to a registration app, typically developed in-house
     * in order to allow new users to sign-up for duo functionality.
     * If the user account status requires enrollment and this link
     * is specified, CAS will redirect the authentication flow
     * to this registration app. Otherwise, the default duo mechanism
     * for new-user registrations shall take over.
     */
    private String registrationUrl;

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Duo integration mode to indicate whether
     * duo-web or duo universal prompt should be used.
     * <p>
     * Accepted values are:
     * {@code BASIC}: Uses the web-sdk to handle duo integrations inside an iFrame.
     * {@code UNIVERSAL}: Handles the integration using Universal Prompt.
     */
    private DuoSecurityIntegrationModes mode = DuoSecurityIntegrationModes.WEBSDK;

    public DuoSecurityMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }

    /**
     * Duo Security integration modes and options.
     */
    public enum DuoSecurityIntegrationModes {
        /**
         * Default integration mode using the web-sdk and iFrames.
         */
        WEBSDK,
        /**
         * Universal Prompt mode via OIDC.
         */
        UNIVERSAL
    }
}
