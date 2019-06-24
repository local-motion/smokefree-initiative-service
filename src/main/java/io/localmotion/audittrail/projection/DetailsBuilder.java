package io.localmotion.audittrail.projection;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsBuilder {

    private final Map<String, Object> details = new HashMap<>();

    private final String objectKey;
    private final DetailsBuilder parentBuilder;

    private DetailsBuilder(String objectKey, DetailsBuilder parentBuilder) {
        this.objectKey = objectKey;
        this.parentBuilder = parentBuilder;
    }


    public static DetailsBuilder instance() {
        return new DetailsBuilder(null, null);
    }

    public DetailsBuilder add(String key, Object value) {
        details.put(key, value);
        return this;
    }

    public DetailsBuilder addObject(String key) {
        return new DetailsBuilder(key, this);
    }

    public DetailsBuilder closeObject() {
        parentBuilder.details.put(objectKey, details);
        return parentBuilder;
    }

    public String build() {
        return new Gson().toJson(details);
    }


    public static class ListBuilder {
        private List<Object> list = new ArrayList<>();

        private final String objectKey;
        private final DetailsBuilder parentBuilder;

        private ListBuilder(String objectKey, DetailsBuilder parentBuilder) {
            this.objectKey = objectKey;
            this.parentBuilder = parentBuilder;
        }

        public ListBuilder add(Object item) {
            list.add(item);
            return this;
        }

        public DetailsBuilder close() {
            parentBuilder.details.put(objectKey, list);
            return parentBuilder;
        }

    }

}

