package ch.rmy.curlcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class CommandLineBuilder {

    private static final String OPTION_PATTERN = "^-{1,2}[a-zA-Z0-9][a-zA-Z0-9-_]*$";

    private final String command;
    private final List<String> arguments = new ArrayList<>();

    public CommandLineBuilder(String command) {
        this.command = command;
    }

    public CommandLineBuilder argument(Object argument) {
        arguments.add(escapeIfNecessary(argument.toString()));
        return this;
    }

    private String escapeIfNecessary(String string) {
        return needsEscaping(string) ? escape(string) : string;
    }

    private boolean needsEscaping(String string) {
        return string.contains("\"") || string.contains(" ");
    }

    private String escape(String string) {
        return "\"" + string.replaceAll("\\\\", Matcher.quoteReplacement("\\")).replaceAll("\"", Matcher.quoteReplacement("\\\"")) + "\"";
    }

    public CommandLineBuilder option(String option, Object... arguments) {
        if (!option.matches(OPTION_PATTERN)) {
            throw new IllegalArgumentException();
        }
        this.arguments.add(option);
        for (Object argument : arguments) {
            argument(argument);
        }
        return this;
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(command);
        for (String argument : arguments) {
            stringBuilder.append(' ');
            stringBuilder.append(argument);
        }
        return stringBuilder.toString();
    }

}
