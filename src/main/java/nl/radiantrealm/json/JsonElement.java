package nl.radiantrealm.json;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public abstract class JsonElement {

    public abstract JsonElement deepCopy();

    public JsonArray getAsJsonArray() {
        if (this instanceof JsonArray jsonArray) {
            return jsonArray;
        }

        throw new IllegalStateException("Not an instance of JSON Array.");
    }

    public boolean isJsonArray() {
        return this instanceof JsonArray;
    }

    public JsonNull getAsJsonNull() {
        if (this instanceof JsonNull jsonNull) {
            return jsonNull;
        }

        throw new IllegalStateException("Not an instance of JSON Null.");
    }

    public boolean isJsonNull() {
        return this instanceof JsonNull;
    }

    public JsonObject getAsJsonObject() {
        if (this instanceof JsonObject jsonObject) {
            return jsonObject;
        }

        throw new IllegalStateException("Not an instance of JSON Object.");
    }

    public boolean isJsonObject() {
        return this instanceof JsonObject;
    }

    public JsonPrimitive getAsJsonPrimitive() {
        if (this instanceof JsonPrimitive jsonPrimitive) {
            return jsonPrimitive;
        }

        throw new IllegalStateException("Not an instance of JSON Primitive.");
    }

    public boolean isJsonPrimitive() {
        return this instanceof JsonPrimitive;
    }

    public boolean getAsBoolean() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public Number getAsNumber() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public String getAsString() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public UUID getAsUUID() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public <T extends Enum<T>> Enum<T> getAsEnum(Class<T> enumerator) {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    public BigDecimal getAsBigDecimal() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        writeJson(this, sb);
        return sb.toString();
    }

    private void writeJson(JsonElement element, StringBuilder sb) {
        if (element == null || element.isJsonNull()) {
            sb.append("null");
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isString()) {
                sb.append('"').append(escape(prim.getAsString())).append('"');
            } else {
                sb.append(prim.getAsString()); // number/boolean
            }
        } else if (element.isJsonObject()) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
                if (!first) sb.append(",");
                sb.append('"').append(escape(e.getKey())).append('"').append(":");
                writeJson(e.getValue(), sb);
                first = false;
            }
            sb.append("}");
        } else if (element.isJsonArray()) {
            sb.append("[");
            boolean first = true;
            for (JsonElement e : element.getAsJsonArray()) {
                if (!first) sb.append(",");
                writeJson(e, sb);
                first = false;
            }
            sb.append("]");
        }
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n"); // minimal escaping
    }
}
