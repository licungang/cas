package org.apereo.cas.trusted.web.flow;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.core.OrderComparator;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default {@link DeviceFingerprintStrategy} implementation that uses {@link DeviceFingerprintComponent} to generate
 * a fingerprint.
 *
 * @author Daniel Frett
 * @since 5.3.0
 */
@AllArgsConstructor
public class DefaultDeviceFingerprintStrategy implements DeviceFingerprintStrategy {
    @NonNull
    private final List<DeviceFingerprintComponent> componentStrategies;
    @NonNull
    private final String componentSeparator;

    @Override
    public String determineFingerprint(final String principal, final RequestContext context, final boolean isNew) {
        return componentStrategies.stream()
                .sorted(OrderComparator.INSTANCE)
                .map(component -> component.determineComponent(principal, context, isNew))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining(componentSeparator));
    }
}
