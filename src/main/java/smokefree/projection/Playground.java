package smokefree.projection;

import lombok.Value;
import smokefree.domain.Status;

@Value
public class Playground {
    String id;
    String name;
    Double lat;
    Double lng;
    Status status;
}
