package io.electroma.sitemap.parse;

import com.google.common.io.ByteStreams;
import io.electroma.sitemap.api.ImmutableVisitResult;
import io.electroma.sitemap.api.PageParser;
import io.electroma.sitemap.api.VisitResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static io.electroma.sitemap.api.VisitResult.Status.OK;
import static java.util.stream.Collectors.toSet;

public class JSoupPageParser implements PageParser {

    private static final int DEFAULT_RESPONSE_LIMIT = 1_000_000;

    @Override
    public VisitResult parse(final String url,
                             final String charset,
                             final InputStream contentStream) throws IOException {
        final InputStream content = ByteStreams.limit(contentStream, DEFAULT_RESPONSE_LIMIT);
        // TODO: better handling of large pages: need to add partial success or apply separate strategy
        final Document document = Jsoup.parse(content, charset, url);
        final Set<String> outLinks = document.select("a").stream()
                .map(href -> href.attr("abs:href"))
                // remove self links - it's poinless to report them
                .filter(href -> !url.equals(href))
                .collect(toSet());

        final Set<String> img = document.select("img").stream()
                .map(href -> href.attr("abs:src"))
                .filter(href -> !isNullOrEmpty(href))
                .collect(toSet());

        final Set<String> css = document.select("link[type=text/css]").stream()
                .map(href -> href.attr("abs:href"))
                .filter(href -> !isNullOrEmpty(href))
                .collect(toSet());

        return ImmutableVisitResult.builder()
                .url(url)
                .status(OK)
                .addAllOutLinks(outLinks)
                .addAllImages(img)
                .addAllCss(css)
                .build();
    }

}
