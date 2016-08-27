package io.electroma.sitemap;

import com.google.common.base.Throwables;
import io.electroma.sitemap.api.ImmutableVisitResult;
import io.electroma.sitemap.api.SitemapBuilder;
import io.electroma.sitemap.api.SitemapStore;
import io.electroma.sitemap.api.VisitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;

public class CrawlingSitemapBuilder implements SitemapBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlingSitemapBuilder.class);

    private final Executor executor;

    private final Function<String, SitemapStore> storeFactory;

    private final Predicate<String> urlFilter;

    private final Function<String, VisitResult> visitor;

    public CrawlingSitemapBuilder(final Executor executor,
                                  final Function<String, SitemapStore> storeFactory,
                                  final Predicate<String> urlFilter,
                                  final Function<String, VisitResult> visitor) {
        this.executor = executor;
        this.storeFactory = storeFactory;
        this.urlFilter = urlFilter;
        this.visitor = visitor;
    }

    public Void build(final String entryUrl) {
        final SitemapStore store = storeFactory.apply(entryUrl);
        try {
            return processVisitResult(fetchLink(entryUrl), store).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to complete crawling", e);
            throw Throwables.propagate(e);
        }
    }

    private CompletableFuture<Void> processVisitResult(final VisitResult inputResult,
                                                       final SitemapStore store) {
        final ImmutableVisitResult processedResult = ImmutableVisitResult.copyOf(inputResult)
                .withOutLinks(inputResult.outLinks().stream().filter(urlFilter).collect(Collectors.toSet()));

        store.addVisitResult(processedResult);
        final List<CompletableFuture<Void>> subExecutions = processedResult.outLinks().stream()
                .peek(url -> LOGGER.info("About to check url " + url))
                .filter(store::tryStartProcessing)
                .map(link ->
                        supplyAsync(() -> fetchLink(link), executor)
                                .thenCompose(res -> processVisitResult(res, store)))
                .collect(toList());
        return CompletableFuture.allOf(subExecutions.toArray(new CompletableFuture[0]));
    }

    private VisitResult fetchLink(final String entryUrl) {
        LOGGER.info("Fetching url " + entryUrl);
        return visitor.apply(entryUrl);
    }
}
