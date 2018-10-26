package smokefree.graphql;

import lombok.SneakyThrows;
import lombok.Value;

import java.util.concurrent.CompletableFuture;

@Value
public class InputAcceptedResponse {
    String id;

    @SneakyThrows
    public static InputAcceptedResponse fromFuture(CompletableFuture<String> result) {
        final String guid = result.get();
        return new InputAcceptedResponse(guid);
    }
}
