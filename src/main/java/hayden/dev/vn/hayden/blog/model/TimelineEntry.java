package hayden.dev.vn.hayden.blog.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimelineEntry {
    private String period;
    private String title;
    private String organization;
    private String description;
    private String type;
}
