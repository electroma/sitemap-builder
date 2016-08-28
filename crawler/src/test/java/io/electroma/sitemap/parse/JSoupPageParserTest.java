package io.electroma.sitemap.parse;

import com.google.common.collect.ImmutableSet;
import io.electroma.sitemap.api.ImmutableVisitResult;
import io.electroma.sitemap.api.VisitResult;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JSoupPageParserTest {

    @Test
    public void checkImageExtract() throws Exception {
       assertEquals(
                ImmutableSet.of("http://whatever/sub/src2.jpg", "http://whatever/../../bad"),
                parse("<img /><img src=\"/sub/src2.jpg\" /><img src=\"../../bad\" />").images());

    }

    @Test
    public void checkCssExtract() throws Exception {
       assertEquals(
                ImmutableSet.of("http://whatever/good.css"),
                parse("<link href=\"notype\" /><link type=\"text/css\" />" +
                        "<link type=\"text/css\" href=\"good.css\" />").css());
    }

    @Test
    public void checkPartialCut() throws Exception {
       assertEquals(
                ImmutableVisitResult.builder().addImages("http://whatever/some.jpg")
                        .url("http://whatever").status(VisitResult.Status.OK).build(),
                parse("<img src=\"some.jpg\" /><link type=\"text/css\" />" +
                        "<link type=\"text"));
    }


    private VisitResult parse(final String input) throws IOException {
        return new JSoupPageParser().parse("http://whatever", "UTF-8",
                new ByteArrayInputStream(input.getBytes("UTF-8")));
    }
}