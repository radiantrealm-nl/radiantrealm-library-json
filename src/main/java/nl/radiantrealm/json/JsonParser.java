package nl.radiantrealm.json;

import java.math.BigDecimal;

public class JsonParser {
    private final String input;
    private int index = 0;

    public JsonParser(String input) {
        this.input = input;
    }

    public JsonElement parse() throws JsonException {
        return switch (advance(true)) {
            case '{' -> getJsonObject();
            case '[' -> getJsonArray();
            default -> throw new IllegalArgumentException();
        };
    }

    public char advance(boolean skipWhiteSpace) {
        while (index < input.length()) {
            char c = input.charAt(index);

            if (skipWhiteSpace && Character.isWhitespace(c)) {
                index++;
            } else {
                index++;
                return c;
            }
        }

        throw new ArrayIndexOutOfBoundsException("Unexpected end of Json input at index " + index);
    }

    public char peek(boolean skipWhiteSpace) {
        int i = index;

        if (skipWhiteSpace) {
            while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
                i++;
            }
        }

        if (i >= input.length()) {
            throw new ArrayIndexOutOfBoundsException("Unexpected end of Json input at index " + i);
        }

        return input.charAt(i);
    }

    public JsonObject getJsonObject() throws JsonException {
        if (advance(true) != '{') {
            throw new JsonException("Not a Json object.");
        }

        return getJsonObject(new JsonObject());
    }

    private JsonObject getJsonObject(JsonObject object) throws JsonException {
        return switch (advance(true)) {
            case '"' -> {
                String key = getJsonString(new StringBuilder());

                if (advance(true) != ':') {
                    throw new JsonException(String.format("Invalid Json format at %s", index));
                }

                char c = advance(true);

                switch (c) {
                    case '"' -> object.add(key, getJsonString(new StringBuilder()));
                    case 't', 'f' -> object.add(key, getJsonBoolean(c == 't'));
                    case 'n' -> object.add(key, getJsonNull());
                    case '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
                        if (c == '-') {
                            c = advance(false);
                        }

                        object.add(key, getJsonNumber(c));
                    }

                    default -> throw new JsonException("Invalid json element for json object.");
                }

                yield getJsonObject(object);
            }

            case ',' -> getJsonObject(object);

            case '}' -> object;
            default -> throw new JsonException("Reader error.");
        };
    }

    public JsonArray getJsonArray() throws JsonException {
        if (advance(true) != '[') {
            throw new JsonException("Not a Json array.");
        }

        return getJsonArray(new JsonArray());
    }

    private JsonArray getJsonArray(JsonArray array) throws JsonException {
        if (peek(true) == ']') {
            advance(true);
            return array;
        }

        while (true) {
            char c = peek(true);

            switch (c) {
                case '"' -> {
                    advance(true);
                    array.add(getJsonString(new StringBuilder()));
                }

                case 't', 'f' -> {
                    advance(true);
                    array.add(getJsonBoolean(c == 't'));
                }

                case 'n' -> {
                    advance(true);
                    array.add(getJsonNull());
                }

                case '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
                    advance(true);

                    if (c == '-') {
                        c = advance(false);
                    }

                    array.add(getJsonNumber(c));
                }

                case '{' -> array.add(getJsonObject());
                case '[' -> array.add(getJsonArray());
                default -> {
                    System.out.println(c);
                    throw new JsonException("Invalid Json element for Json Array at index " + index);
                }
            }

            char next = advance(true);

            if (next == ']') {
                return array;
            }

            if (next != ',') {
                throw new JsonException("Expected a comma or closing bracket in array at index " + index);
            }
        }

//        switch (c) {
//            case '"' -> array.add(getJsonString(new StringBuilder()));
//            case 't', 'f' -> array.add(getJsonBoolean(c == 't'));
//            case 'n' -> array.add(getJsonNull());
//            case '-', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' -> {
//                if (c == '-') {
//                    c = advance(false);
//                }
//
//                array.add(getJsonNumber(c));
//            }
//
//            case '{' -> array.add(getJsonObject(new JsonObject()));
//            case '[' -> array.add(getJsonArray(new JsonArray()));
//            case ']' -> {
//                return array;
//            }
//            case ',' -> getJsonArray(array);
//            default -> throw new JsonException("Invalid Json element for json array.");
//        }
//
//        return getJsonArray(array);
    }

    private String getJsonString(StringBuilder builder) {
        char c = advance(false);

        if (c == '"') {
            return builder.toString();
        } else {
            return getJsonString(builder.append(c));
        }
    }

    private boolean getJsonBoolean(boolean firstCharacter) throws JsonException {
        StringBuilder builder = new StringBuilder(firstCharacter ? "T" : "F");
        builder.append(advance(false));
        builder.append(advance(false));
        builder.append(advance(false));

        if (!firstCharacter) {
            builder.append(advance(false));
        }

        if (builder.toString().equalsIgnoreCase(String.valueOf(firstCharacter))) {
            return firstCharacter;
        } else {
            throw new JsonException("Invalid boolean value.");
        }
    }

    private JsonNull getJsonNull() throws JsonException {
        StringBuilder builder = new StringBuilder("N");
        builder.append(advance(false));
        builder.append(advance(false));
        builder.append(advance(false));

        if (builder.toString().equalsIgnoreCase("NULL")) {
            return JsonNull.INSTANCE;
        } else {
            throw new JsonException("Invalid null value.");
        }
    }

    private Number getJsonNumber(int firstDigit) {
        String numberString = getJsonNumber(new StringBuilder(String.valueOf(firstDigit)));
        BigDecimal decimal = new BigDecimal(numberString);

        if (decimal.scale() < 1) {
            long value = decimal.longValue();

            if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                return (short) value;
            }

            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }

            return value;
        }

        return decimal;
    }

    private String getJsonNumber(StringBuilder builder) {
        char c = peek(false);

        return switch (c) {
            case '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                if (index++ >= input.length()) {
                    throw new ArrayIndexOutOfBoundsException();
                }

                yield getJsonNumber(builder.append(c));
            }

            default -> builder.toString();
        };
    }
}
