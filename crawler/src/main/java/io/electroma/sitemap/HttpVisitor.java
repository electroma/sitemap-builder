package io.electroma.sitemap;

import io.electroma.sitemap.api.ImmutableVisitResult;
import io.electroma.sitemap.api.PageParser;
import io.electroma.sitemap.api.VisitResult;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Function;

import static com.google.common.base.MoreObjects.firstNonNull;
import static io.electroma.sitemap.api.VisitResult.Status.FAIL;
import static io.electroma.sitemap.api.VisitResult.Status.UNPARSABLE;

public class HttpVisitor implements Function<String, VisitResult> {

    public static final String TEXT_HTML = "text/html";

    private static final int DEFAULT_SOCKET_TIMEOUT = 5_000;

    private static final int DEFAULT_CONNECT_TIMEOUT = 5_000;

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static final int DEFAULT_MAX_PER_ROUTE = 5;

    //TODO: need to configure it base on target site and application configuration
    private CloseableHttpClient httpclient = createClient(DEFAULT_MAX_PER_ROUTE);

    private final PageParser parser;

    public HttpVisitor(final PageParser parser) {
        this.parser = parser;
    }

    private CloseableHttpClient createClient(final int maxPerRoute) {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(maxPerRoute);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    @Override
    public VisitResult apply(final String url) {
        final HttpGet request = new HttpGet(url);

        try {
            final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

            requestConfigBuilder.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);
            requestConfigBuilder.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
            // TODO: max redirect count
            httpClientBuilder.setRedirectStrategy(new DefaultRedirectStrategy());
            //TODO: retry policy?

            try(final CloseableHttpResponse response = httpclient.execute(request)) {
                final ContentType contentType = ContentType.get(response.getEntity());
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                        && contentType != null
                        && TEXT_HTML.equals(contentType.getMimeType())) {
                    final String charset = firstNonNull(contentType.getCharset(),
                            DEFAULT_CHARSET).name();
                    final InputStream contentStream = response.getEntity().getContent();
                    return parser.parse(url, charset, contentStream);
                } else {
                    return ImmutableVisitResult.of(url, UNPARSABLE);
                }
            }
        } catch (final IOException exception) {
            return ImmutableVisitResult.of(url, FAIL);
        }
    }
}
