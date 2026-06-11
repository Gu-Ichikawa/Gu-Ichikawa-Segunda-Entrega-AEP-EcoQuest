package ecoquest.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Json {
    private Json() {
    }

    public static StringBuilder prop(StringBuilder builder, String key, String value) {
        return builder.append("\"").append(escape(key)).append("\":\"").append(escape(value)).append("\"");
    }

    public static StringBuilder prop(StringBuilder builder, String key, int value) {
        return builder.append("\"").append(escape(key)).append("\":").append(value);
    }

    public static StringBuilder prop(StringBuilder builder, String key, boolean value) {
        return builder.append("\"").append(escape(key)).append("\":").append(value);
    }

    public static String arrayStrings(List<String> valores) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) builder.append(",");
            builder.append("\"").append(escape(valores.get(i))).append("\"");
        }
        builder.append("]");
        return builder.toString();
    }

    public static String getString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return "";
        }
        return unescape(matcher.group(1));
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String unescape(String value) {
        return value
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
