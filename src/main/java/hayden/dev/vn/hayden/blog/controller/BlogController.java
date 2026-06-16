package hayden.dev.vn.hayden.blog.controller;

import hayden.dev.vn.hayden.blog.model.BlogPost;
import hayden.dev.vn.hayden.blog.service.BlogService;
import hayden.dev.vn.hayden.blog.service.MarkdownService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private static final int PAGE_SIZE = 6;

    private final BlogService blogService;
    private final MarkdownService markdownService;

    public BlogController(BlogService blogService, MarkdownService markdownService) {
        this.blogService = blogService;
        this.markdownService = markdownService;
    }

    @GetMapping
    public String listPosts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        List<BlogPost> filtered = blogService.searchPosts(q);
        int total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        int currentPage = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, total);
        List<BlogPost> pagePosts = filtered.subList(fromIndex, toIndex);

        model.addAttribute("posts", pagePosts);
        model.addAttribute("query", q);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalPosts", total);
        model.addAttribute("pageTitle", "Blog - Bài viết về Java & Backend");
        model.addAttribute("pageDescription", "Tổng hợp các bài viết về Java, Spring Boot, Architecture, DevOps và Backend Development.");
        return "blog/list";
    }

    @GetMapping("/{slug}")
    public String postDetail(@PathVariable String slug, Model model) {
        BlogPost post = blogService.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại"));

        model.addAttribute("post", post);
        model.addAttribute("content", markdownService.render(post.getContent()));
        model.addAttribute("relatedPosts", blogService.getRelatedPosts(post, 3));
        model.addAttribute("pageTitle", post.getTitle());
        model.addAttribute("pageDescription", post.getExcerpt());
        model.addAttribute("pageOgImage", post.getCoverImage());
        return "blog/detail";
    }
}
