package smokefree.graphql;

import lombok.Value;

@Value
public class Article {
    private String id;
    private String title;
    private String text;
    private Long authorId;
}
