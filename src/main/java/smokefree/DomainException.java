package smokefree;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class DomainException extends RuntimeException implements GraphQLError {
    private String code;
    private String niceMessage;

    public DomainException(String code, String technicalMessage, String niceMessage) {
        super(technicalMessage);
        this.code = code;
        this.niceMessage = niceMessage;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return null;
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> extensions = newHashMap();
        extensions.put("code", code);
        extensions.put("niceMessage", niceMessage);
        return extensions;
    }
}
