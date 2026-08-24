package fr.insalyon.creatis.vip.api.model.serializing;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InputValuesDeserializer extends StdDeserializer<List<Map<String, Object>>> {

    public InputValuesDeserializer() {
        super(List.class);
    }

    @Override
    public List<Map<String, Object>> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JavaType mapType = ctx.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        // Array of dictionaries
        if (p.currentToken() == JsonToken.START_ARRAY) {
            List<Map<String, Object>> list = new ArrayList<>();
            p.nextToken();
            while (p.currentToken() != JsonToken.END_ARRAY) {
                list.add(ctx.readValue(p, mapType));
                p.nextToken();
            }

            return list;
        // Unique dictionary
        } else if (p.currentToken() == JsonToken.START_OBJECT) {
            Map<String, Object> map = ctx.readValue(p, mapType);
            return Collections.singletonList(map);
        } else {
            throw ctx.wrongTokenException(p, List.class, JsonToken.START_ARRAY,
                    "InputValues must be a dictionary or an array of dictionaries");
        }
    }
}
