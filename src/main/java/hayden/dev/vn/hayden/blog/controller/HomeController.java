package hayden.dev.vn.hayden.blog.controller;

import hayden.dev.vn.hayden.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BlogService blogService;

    @Value("${blog.author.name}") private String authorName;
    @Value("${blog.author.title}") private String authorTitle;
    @Value("${blog.author.email}") private String authorEmail;
    @Value("${blog.author.github}") private String github;
    @Value("${blog.author.linkedin}") private String linkedin;
    @Value("${blog.author.facebook}") private String facebook;

    public HomeController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentPosts", blogService.getRecentPosts(6));
        model.addAttribute("pageTitle", authorName + " - " + authorTitle);
        model.addAttribute("pageDescription",
                "Blog cá nhân của " + authorName + " - " + authorTitle + ". Chia sẻ về Java, Spring Boot, Architecture và các chủ đề backend development.");
        model.addAttribute("authorName", authorName);
        model.addAttribute("authorTitle", authorTitle);
        model.addAttribute("authorEmail", authorEmail);
        model.addAttribute("github", github);
        model.addAttribute("linkedin", linkedin);
        model.addAttribute("facebook", facebook);
        return "index";
    }
}
