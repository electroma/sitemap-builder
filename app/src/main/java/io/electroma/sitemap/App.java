package io.electroma.sitemap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.electroma.sitemap.api.SitemapStore;
import io.electroma.sitemap.api.VisitResult;
import io.electroma.sitemap.output.ConsoleSiteMapVisualizer;
import io.electroma.sitemap.parse.JSoupPageParser;

import java.net.MalformedURLException;
import java.net.URL;
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

    @Parameter(names = {"--css"}, description = "include css links into result (default false)")
    private boolean includeCss;

    @Parameter(names = {"--img"}, description = "include images into result (default false)")
    private boolean includeImg;

    private final ConsoleSiteMapVisualizer simeMapVizualizer;

    public App(final ConsoleSiteMapVisualizer simeMapVizualizer) {
        this.simeMapVizualizer = simeMapVizualizer;
    }

    public static void main(final String[] args) {
        final App app = new App(new ConsoleSiteMapVisualizer(System.out));
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
                new HttpVisitor(new JSoupPageParser())).build(startUrl);

        final Map<String, VisitResult> all = sitemapStore.getAll();
        simeMapVizualizer.visualize(all, startUrl, includeImg, includeCss);
        executor.shutdownNow();
    }

}
