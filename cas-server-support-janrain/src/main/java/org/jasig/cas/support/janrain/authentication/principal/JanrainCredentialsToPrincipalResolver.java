/*
 *  Copyright 2012 The JA-SIG Collaborative
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jasig.cas.support.janrain.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class resolves the principal (OpenID URL) from the Janrain credential (token that is passed to the auth_info webservice)
 * 
 * @author Eric Pierce
 * @since 3.5.0
 */
public final class JanrainCredentialsToPrincipalResolver extends AbstractPersonDirectoryCredentialsToPrincipalResolver
    implements CredentialsToPrincipalResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(JanrainCredentialsToPrincipalResolver.class);
    
    @Override
    protected String extractPrincipalId(final Credentials credentials) {
        JanrainCredentials janrainCredentials = (JanrainCredentials) credentials;
        String principal = janrainCredentials.getIdentifier();
        logger.debug("[Principal : {}", principal);
        return principal;
    }
    
    /**
     * Return true if Credentials are OAuthCredentials, false otherwise.
     */
    public boolean supports(final Credentials credentials) {
        return credentials != null && (JanrainCredentials.class.isAssignableFrom(credentials.getClass()));
    }
}
