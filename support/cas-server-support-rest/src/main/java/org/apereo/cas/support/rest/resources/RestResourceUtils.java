package org.apereo.cas.support.rest.resources;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link RestResourceUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@UtilityClass
public class RestResourceUtils {

    private static final ObjectMapper MAPPER;

    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";
    private static final String NO_MESSAGE = "No message found";

    static {
        MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    /**
     * Create response entity for authn failure response.
     *
     * @param e the e
     * @param request the http request
     * @return the response entity
     */
    public static ResponseEntity<String> createResponseEntityForAuthnFailure(final AuthenticationException e,
                                                                             final HttpServletRequest request) {
        try {
            val authnExceptions = e.getHandlerErrors().values()
                .stream()
                .map(ex -> ex.getClass().getSimpleName()
                     + ": "
                     + StringUtils.defaultIfBlank(ex.getMessage(), "Authentication Failure: " + e.getMessage())
                     + ": "
                     + getMessage(ex.getClass().getSimpleName(), request))
                .collect(Collectors.toList());
            val errorsMap = new HashMap<String, List<String>>();
            errorsMap.put("authentication_exceptions", authnExceptions);
            LOGGER.warn("[{}] Caused by: [{}]", e.getMessage(), authnExceptions);

            return new ResponseEntity<>(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(errorsMap), HttpStatus.UNAUTHORIZED);
        } catch (final JsonProcessingException exception) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getMessage(final String className, final HttpServletRequest request) {
        try {
            return ApplicationContextProvider
                .getApplicationContext()
                .getMessage(DEFAULT_MESSAGE_BUNDLE_PREFIX + className, null, request.getLocale());
        } catch (final Exception e) {
            return NO_MESSAGE;
        }
    }
}
