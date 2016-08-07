package ch.rmy.android.http_shortcuts.variables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Variables {

    public static final int PREFIX_LENGTH = 2;
    public static final int SUFFIX_LENGTH = 2;
    private static final String REGEX = "\\{\\{[A-Za-z][A-Za-z0-9]*\\}\\}";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static Matcher match(CharSequence s) {
        return PATTERN.matcher(s);
    }

    public static String insert(String string, ResolvedVariables variables) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = Variables.match(string);
        int previousEnd = 0;
        while (matcher.find()) {
            builder.append(string.substring(previousEnd, matcher.start()));
            String placeholder = string.substring(matcher.start(), matcher.end());
            String variableName = string.substring(matcher.start() + PREFIX_LENGTH, matcher.end() - SUFFIX_LENGTH);
            builder.append(variables.hasValue(variableName) ? variables.getValue(variableName) : placeholder);
            previousEnd = matcher.end();
        }
        builder.append(string.substring(previousEnd, string.length()));
        return builder.toString();
    }

}
