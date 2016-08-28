package io.electroma.sitemap.api;

import java.io.IOException;
import java.io.InputStream;

public interface PageParser {
    VisitResult parse(String url,
                      String charset,
                      InputStream contentStream) throws IOException;
}
