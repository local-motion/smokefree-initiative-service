package smokefree.domain;

import lombok.Getter;
import lombok.Value;

@Value
public class Playground {
    String id;
    String name;
    Double lat;
    Double lng;
}
