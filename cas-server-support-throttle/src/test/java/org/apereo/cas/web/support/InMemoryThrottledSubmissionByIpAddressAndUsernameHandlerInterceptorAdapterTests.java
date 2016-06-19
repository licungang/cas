package org.apereo.cas.web.support;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Unit test for {@link InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations={"classpath:/inMemoryThrottledSubmissionContext.xml"})
public class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapterTests
extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapterTests {

    @Autowired
    private InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter throttle;

    @Override
    protected AbstractThrottledSubmissionHandlerInterceptorAdapter getThrottle() {
        return throttle;
    }
}
