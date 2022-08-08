package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.HttpBasedServiceCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("FileSystem")
public class RejectUsersAuthenticationHandlerTests {
    private final RejectUsersAuthenticationHandler authenticationHandler;

    public RejectUsersAuthenticationHandlerTests() {
        val users = new HashSet<String>();
        users.add("scott");
        users.add("dima");
        users.add("bill");
        authenticationHandler = new RejectUsersAuthenticationHandler(StringUtils.EMPTY, mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), users);
    }

    @Test
    public void verifySupportsProperUserCredentials() throws Exception {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("fff");
        credential.assignPassword("rutgers");
        assertNotNull(authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifyDoesntSupportBadUserCredentials() {
        try {
            assertFalse(authenticationHandler
                .supports(new HttpBasedServiceCredential(new URL(
                    "http://www.rutgers.edu"), CoreAuthenticationTestUtils.getRegisteredService())));
        } catch (final MalformedURLException e) {
            throw new AssertionError("Could not resolve URL.");
        }
    }

    @Test
    public void verifyFailsUserInMap() {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("scott");
        credential.assignPassword("rutgers");
        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifyPassesUserNotInMap() throws Exception {
        val credential = new UsernamePasswordCredential();
        credential.setUsername("fds");
        credential.assignPassword("rutgers");
        assertNotNull(authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifyPassesNullUserName() {
        val credential = new UsernamePasswordCredential();
        credential.setUsername(null);
        credential.assignPassword("user");
        assertThrows(AccountNotFoundException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));
    }

    @Test
    public void verifyPassesNullUserNameAndPassword() {
        assertThrows(AccountNotFoundException.class, () -> authenticationHandler.authenticate(new UsernamePasswordCredential(), mock(Service.class)));
    }
}
