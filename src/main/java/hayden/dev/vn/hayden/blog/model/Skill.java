package hayden.dev.vn.hayden.blog.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Skill {
    private String name;
    private String category;
    private int level;
}
