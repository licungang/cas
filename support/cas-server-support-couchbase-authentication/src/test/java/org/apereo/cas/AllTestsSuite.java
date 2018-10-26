
package org.apereo.cas;

import org.apereo.cas.authentication.CouchbaseAuthenticationHandlerTests;
import org.apereo.cas.authentication.CouchbasePersonAttributeDaoTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Auto-generated by Gradle Build
 * @since 6.0.0-RC3
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CouchbasePersonAttributeDaoTests.class,
    CouchbaseAuthenticationHandlerTests.class
})
public class AllTestsSuite {
}
