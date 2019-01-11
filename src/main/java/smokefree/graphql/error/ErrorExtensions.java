package smokefree.graphql.error;

import lombok.Value;

@Value
public class ErrorExtensions {
    ErrorCode code;
    String niceMessage;
}
