package org.apereo.cas.web.flow.passwordless;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.config.SurrogateAuthenticationPasswordlessConfiguration;
import org.apereo.cas.web.flow.action.BaseSurrogateAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogatePasswordlessAuthenticationRequestParserTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    SurrogateAuthenticationPasswordlessConfiguration.class,
    BaseSurrogateAuthenticationTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@Tag("Delegation")
public class SurrogatePasswordlessAuthenticationRequestParserTests extends BaseSurrogateAuthenticationTests {
    @Autowired
    @Qualifier(PasswordlessRequestParser.BEAN_NAME)
    private PasswordlessRequestParser passwordlessRequestParser;

    @Test
    public void verifySurrogateRequest() {
        val results = passwordlessRequestParser.parse("user3+casuser");
        assertEquals("casuser", results.getUsername());
    }

    @Test
    public void verifyDefaultRequest() {
        val results = passwordlessRequestParser.parse("casuser@example.org");
        assertEquals("casuser@example.org", results.getUsername());
    }
}
