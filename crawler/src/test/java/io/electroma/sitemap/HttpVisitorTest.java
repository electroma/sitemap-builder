package io.electroma.sitemap;

import com.sun.net.httpserver.HttpServer;
import io.electroma.sitemap.api.VisitResult;
import io.electroma.sitemap.parse.JSoupPageParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

public class HttpVisitorTest {

    private static HttpServer server;
    volatile static ServerConfig serverConfig = new ServerConfig();

    @BeforeClass
    public static void setUp() throws IOException {

        server = HttpServer.create(new InetSocketAddress(9999), 1);
        server.createContext("/", httpExchange -> {
            httpExchange.getResponseHeaders().set("Content-Type", serverConfig.mime);
            httpExchange.sendResponseHeaders(serverConfig.code, 0);
            httpExchange.getResponseBody().write(serverConfig.body.getBytes());
            httpExchange.close();
        });
        server.start();
    }

    @Test
    public void applyNonHtml() {
        configureResponse(200, "something", "");
        final VisitResult apply = new HttpVisitor(new JSoupPageParser()).apply("http://localhost:9999");
        assertEquals(VisitResult.Status.UNPARSABLE, apply.getStatus());
    }

    @Test
    public void applyNonOKCode() {
        configureResponse(500, "text/html", "");
        final VisitResult apply = new HttpVisitor(new JSoupPageParser()).apply("http://localhost:9999");
        assertEquals(VisitResult.Status.UNPARSABLE, apply.getStatus());
    }

    @Test
    public void applyHappyPath() {
        configureResponse(200, "text/html", "<a href=\"/test\">test</a>");
        final VisitResult apply = new HttpVisitor(new JSoupPageParser()).apply("http://localhost:9999");
        assertEquals(VisitResult.Status.OK, apply.getStatus());
    }

    @Test
    public void testResponseTimeout() throws Exception {
        //TODO: need to check slow page responses
    }

    @Test
    public void testTooBigResponse() throws Exception {
        // huge pages should not kill fetcher
    }

    private void configureResponse(final int code, final String mime, final String body) {
        serverConfig = new ServerConfig(code, mime, body);
    }

    @AfterClass
    public static void tearDown() {
        server.stop(0);

    }

    private static class ServerConfig {
        public ServerConfig() {
        }

        public ServerConfig(int code, String mime, String body) {
            this.code = code;
            this.mime = mime;
            this.body = body;
        }

        int code = 200;
        String mime = "";
        String body = "";
    }
}