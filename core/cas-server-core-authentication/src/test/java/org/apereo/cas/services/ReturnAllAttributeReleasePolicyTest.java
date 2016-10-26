package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Misagh Moayyed
 * @since 5.0
 */
public class ReturnAllAttributeReleasePolicyTest {

    private static final File JSON_FILE = new File("returnAllAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAReturnAllAttributeReleasePolicyToJson() throws IOException {
        final ReturnAllAttributeReleasePolicy policyWritten = new ReturnAllAttributeReleasePolicy();

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnAllAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
