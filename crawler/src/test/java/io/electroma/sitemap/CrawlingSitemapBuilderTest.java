package io.electroma.sitemap;

import io.electroma.sitemap.api.ImmutableVisitResult;
import io.electroma.sitemap.api.SitemapStore;
import io.electroma.sitemap.api.VisitResult.Status;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class CrawlingSitemapBuilderTest {

    private static final int URL_CNT = 100;

    @Test
    public void build() throws Exception {

        BasicConfigurator.configure();

        final AtomicInteger fetchCnt = new AtomicInteger();

        final SitemapStore store = new SitemapStore();
        final CrawlingSitemapBuilder sitemapBuilder = new CrawlingSitemapBuilder(
                Executors.newFixedThreadPool(3),
                url -> store, url -> true, url -> {
            final ImmutableVisitResult.Builder builder = ImmutableVisitResult.builder();
            final int currIter = fetchCnt.incrementAndGet();
            if (currIter < URL_CNT) {
                builder.addAllOutLinks(IntStream.range(0, URL_CNT)
                        .filter(n -> n != currIter).mapToObj(n -> "url" + n).collect(Collectors.toSet()));
            }
            return builder
                    .url(url)
                    .status(Status.OK).build();
        });

        sitemapBuilder.build("url");
        assertEquals(URL_CNT + 1, fetchCnt.get());
        assertEquals(URL_CNT + 1, store.getAll().size());
        assertEquals(URL_CNT - 1, store.getAll().get("url").outLinks().size());
    }

}