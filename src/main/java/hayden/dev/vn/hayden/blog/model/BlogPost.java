package hayden.dev.vn.hayden.blog.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BlogPost {
    private String id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String coverImage;
    private String category;
    private List<String> tags;
    private LocalDate publishedAt;
    private int readTimeMinutes;
    private boolean featured;
}
