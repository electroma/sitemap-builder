package io.electroma.sitemap.api;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SitemapStore {

    private static final VisitResult INPROGRESS = ImmutableVisitResult.builder()
            .url("url")
            .status(VisitResult.Status.INPROGRESS).build();

    private final ConcurrentMap<String, VisitResult> store = new ConcurrentHashMap<>();

    public void addVisitResult(final VisitResult visitResult) {
        store.put(visitResult.getUrl(), visitResult);
    }

    public boolean hasRecord(final String link) {
        return store.containsKey(link);
    }

    public boolean tryStartProcessing(final String url) {
        return store.putIfAbsent(url, INPROGRESS) == null;
    }

    public Map<String, VisitResult> getAll() {
        return ImmutableMap.copyOf(store);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SitemapStore{");
        sb.append("store=").append(store);
        sb.append('}');
        return sb.toString();
    }
}
