package hayden.dev.vn.hayden.blog.controller;

import hayden.dev.vn.hayden.blog.service.BlogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProjectsController {

    private final BlogService blogService;

    @Value("${blog.author.name}") private String authorName;

    public ProjectsController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("projects", blogService.getProjects());
        model.addAttribute("pageTitle", "Dự án - " + authorName);
        model.addAttribute("pageDescription", "Các dự án cá nhân và open-source của " + authorName + ".");
        return "projects";
    }
}
