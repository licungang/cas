package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class OAuthRegisteredServiceTests {

    private static final File JSON_FILE = new File("oAuthRegisteredService.json");
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ClassPathResource RESOURCE = new ClassPathResource("services");

    private ServiceRegistryDao dao;

    public OAuthRegisteredServiceTests() throws Exception {
        this.dao = new JsonServiceRegistryDao(RESOURCE, false, mock(ApplicationEventPublisher.class));
    }

    @BeforeClass
    public static void prepTests() throws Exception {
        FileUtils.cleanDirectory(RESOURCE.getFile());
    }

    @Test
    public void checkCloning() {
        final AbstractRegisteredService r = new OAuthRegisteredService();
        r.setName("checkCloning");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");

        final OAuthRegisteredService r2 = (OAuthRegisteredService) r.clone();
        assertEquals(r, r2);
    }

    @Test
    public void checkSaveMethod() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("checkSaveMethod");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setClientId("clientid");
        r.setServiceId("secret");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertTrue(r2 instanceof OAuthRegisteredService);
        this.dao.load();
        final RegisteredService r3 = this.dao.findServiceById(r2.getId());
        assertTrue(r3 instanceof OAuthRegisteredService);
        assertEquals(r, r2);
        assertEquals(r2, r3);
    }

    @Test
    public void verifySerializeAOAuthRegisteredServiceToJson() throws IOException {
        final OAuthRegisteredService serviceWritten = new OAuthRegisteredService();
        serviceWritten.setName("checkSaveMethod");
        serviceWritten.setServiceId("testId");
        serviceWritten.setTheme("theme");
        serviceWritten.setDescription("description");
        serviceWritten.setClientId("clientid");
        serviceWritten.setServiceId("secret");
        serviceWritten.setBypassApprovalPrompt(true);

        mapper.writeValue(JSON_FILE, serviceWritten);

        final RegisteredService serviceRead = mapper.readValue(JSON_FILE, OAuthRegisteredService.class);

        assertEquals(serviceWritten, serviceRead);
    }
}
