package hayden.dev.vn.hayden.blog.controller;

import hayden.dev.vn.hayden.blog.model.BlogPost;
import hayden.dev.vn.hayden.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class SeoController {

    private final BlogService blogService;

    @Value("${blog.site.url}") private String siteUrl;

    public SeoController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        return """
                User-agent: *
                Allow: /
                Disallow: /actuator/

                Sitemap: %s/sitemap.xml
                """.formatted(siteUrl);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        List<BlogPost> posts = blogService.getAllPosts();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        appendUrl(sb, siteUrl + "/", "1.0", "weekly");
        appendUrl(sb, siteUrl + "/blog", "0.9", "daily");
        appendUrl(sb, siteUrl + "/about", "0.7", "monthly");
        appendUrl(sb, siteUrl + "/projects", "0.7", "monthly");

        for (BlogPost post : posts) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(siteUrl).append("/blog/").append(post.getSlug()).append("</loc>\n");
            sb.append("    <lastmod>").append(post.getPublishedAt().format(fmt)).append("</lastmod>\n");
            sb.append("    <changefreq>monthly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }
        sb.append("</urlset>");
        return sb.toString();
    }

    private void appendUrl(StringBuilder sb, String loc, String priority, String changefreq) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(loc).append("</loc>\n");
        sb.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sb.append("    <priority>").append(priority).append("</priority>\n");
        sb.append("  </url>\n");
    }
}
