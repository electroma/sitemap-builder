package io.electroma.sitemap.api;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public interface VisitResult {

    enum Status {
        OK, INPROGRESS, UNPARSABLE, FAIL
    }

    @Value.Parameter
    String getUrl();

    @Value.Parameter
    Status getStatus();

    Set<String> outLinks();
    Set<String> scipts();
    Set<String> css();
    Set<String> images();
}
