/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import java.security.cert.X509Certificate;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @version $Revision$ $Date$
 * @since 3.0.6
 *
 */
public class X509CertificateCredentialsToSerialNumberPrincipalResolverTests
    extends AbstractX509CertificateTests {

    private final X509CertificateSerialNumberPrincipalResolver resolver = new X509CertificateSerialNumberPrincipalResolver();
    
    public void testResolvePrincipalInternal() {
        final X509CertificateCredential c = new X509CertificateCredential(new X509Certificate[] {VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        
        assertEquals(VALID_CERTIFICATE.getSerialNumber().toString(), this.resolver.resolve(c).getId());
    }

    public void testSupport() {
        final X509CertificateCredential c = new X509CertificateCredential(new X509Certificate[] {VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(c));
    }
    
    public void testSupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }

}
