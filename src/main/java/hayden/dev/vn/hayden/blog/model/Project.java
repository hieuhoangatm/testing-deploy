package hayden.dev.vn.hayden.blog.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class Project {
    private String id;
    private String title;
    private String description;
    private String image;
    private List<String> technologies;
    private String githubUrl;
    private String demoUrl;
    private boolean featured;
}
