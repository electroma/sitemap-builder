package io.electroma.sitemap.output;

import com.google.common.base.Strings;
import io.electroma.sitemap.api.VisitResult;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsoleSiteMapVisualizer implements SiteMapVisualizer {

    private final PrintStream out;

    public ConsoleSiteMapVisualizer(final PrintStream out) {
        this.out = out;
    }

    public void visualize(final Map<String, VisitResult> all,
                          final String startUrl,
                          final boolean includeImages,
                          final boolean includeCss) {
        print(all, startUrl, new ArrayList<>(), 0, includeImages, includeCss);
    }

    private void print(final Map<String, VisitResult> all,
                       final String startUrl,
                       final List<String> visited,
                       final int level,
                       final boolean includeImages,
                       final boolean includeCss) {
        int idx = visited.indexOf(startUrl);
        final String padding = Strings.repeat(" ", level);
        if (idx == -1) {
            idx = visited.size();
            visited.add(startUrl);
            final VisitResult visitResult = all.get(startUrl);
            out.println(padding + startUrl + " <" + idx + ">");

            if (visitResult != null) {
                if (includeImages) {
                    visitResult.images().forEach(url -> out.println(padding + url + " <IMG>"));
                }
                if (includeCss) {
                    visitResult.css().forEach(url -> out.println(padding + url + " <CSS>"));
                }
                visitResult.outLinks().forEach(href -> print(all, href, visited, level + 1, includeImages, includeCss));
            }
        } else {
            out.println(padding + startUrl + " >>> [" + idx + "]");
        }
    }
}
