package nl.radiantrealm.json;

public class JsonNull extends JsonElement {
    public static final JsonNull INSTANCE = new JsonNull();

    @Override
    public JsonElement deepCopy() {
        return INSTANCE;
    }
}
