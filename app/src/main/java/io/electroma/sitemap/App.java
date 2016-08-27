package io.electroma.sitemap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Strings;
import io.electroma.sitemap.api.SitemapStore;
import io.electroma.sitemap.api.VisitResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkArgument;

public class App {

    public static final int DEFAULT_PARALLEL = 3;

    @Parameter(names = {"-p", "--parallel"}, description = "How many threads will be used to crawl. Default value" + DEFAULT_PARALLEL)
    private int parallel = DEFAULT_PARALLEL;

    @Parameter(names = {"-u", "--url"}, description = "start url", required = true)
    private String startUrl;


    public static void main(final String[] args) {
        final App app = new App();
        new JCommander(app, args);
        app.buildSiteMap();
    }

    private void buildSiteMap() {
        checkArgument(parallel > 0, "Number of threads must be > 0");
        try {
            checkArgument("http".equals(new URL(startUrl).getProtocol()), "Number of threads must be > 0");
        } catch (final MalformedURLException e) {
            System.err.println("Failed to parse input URL " + startUrl);
        }
        final SitemapStore sitemapStore = new SitemapStore();

        final ExecutorService executor = Executors.newFixedThreadPool(parallel);
        new CrawlingSitemapBuilder(executor,
                url -> sitemapStore,
                // TODO: very basic filter - need to add https/http switch + some normalization (reuse from crawler-commons)
                url -> url.startsWith(startUrl),
                new HttpVisitor()).build(startUrl);

        final Map<String, VisitResult> all = sitemapStore.getAll();
        visualize(all, startUrl, new ArrayList<>(), 0);
        executor.shutdownNow();
    }

    private void visualize(final Map<String, VisitResult> all,
                           final String startUrl,
                           final List<String> visited,
                           final int level) {
        int idx = visited.indexOf(startUrl);
        if(idx == -1) {
            idx = visited.size();
            visited.add(startUrl);
            final VisitResult visitResult = all.get(startUrl);
            System.out.println(Strings.repeat(" ", level) + startUrl + " <" + idx + ">");

            if(visitResult != null) {
                visitResult.outLinks().forEach(href -> visualize(all, href, visited, level + 1));
            }
        } else {
            System.out.println(Strings.repeat(" ", level) + startUrl + " >>> [" + idx + "]");
        }
    }
}
