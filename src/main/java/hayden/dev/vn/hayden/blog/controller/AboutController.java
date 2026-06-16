package hayden.dev.vn.hayden.blog.controller;

import hayden.dev.vn.hayden.blog.model.Skill;
import hayden.dev.vn.hayden.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AboutController {

    private final BlogService blogService;

    @Value("${blog.author.name}") private String authorName;
    @Value("${blog.author.github}") private String github;
    @Value("${blog.author.linkedin}") private String linkedin;
    @Value("${blog.author.email}") private String email;

    public AboutController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/about")
    public String about(Model model) {
        List<Skill> skills = blogService.getSkills();
        Map<String, List<Skill>> skillsByCategory = skills.stream()
                .collect(Collectors.groupingBy(Skill::getCategory));

        model.addAttribute("skillsByCategory", skillsByCategory);
        model.addAttribute("timeline", blogService.getTimeline());
        model.addAttribute("authorName", authorName);
        model.addAttribute("github", github);
        model.addAttribute("linkedin", linkedin);
        model.addAttribute("email", email);
        model.addAttribute("pageTitle", "Về tôi - " + authorName);
        model.addAttribute("pageDescription",
                "Tìm hiểu thêm về " + authorName + " - Backend Engineer với đam mê Java, Spring Boot và Cloud Architecture.");
        return "about";
    }
}
