package ch.rmy.curlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


class CommandParser {

    private enum State {
        INIT,
        SINGLE_QUOTE,
        DOUBLE_QUOTE
    }

    static List<String> parseCommand(String command) {
        StringTokenizer tokenizer = new StringTokenizer(command + " ", " '\"\\", true);
        State state = State.INIT;
        ArrayList<String> arguments = new ArrayList<>();

        boolean flush = false;
        StringBuilder builder = new StringBuilder();

        String previousToken = "";
        while (tokenizer.hasMoreTokens()) {
            String currentToken = tokenizer.nextToken();
            switch (state) {
                case SINGLE_QUOTE: {
                    if ("\'".equals(currentToken) && !previousToken.equals("\\")) {
                        state = State.INIT;
                        flush = true;
                    } else {
                        builder.append(currentToken);
                    }
                    break;
                }
                case DOUBLE_QUOTE: {
                    if ("\"".equals(currentToken) && !previousToken.equals("\\")) {
                        state = State.INIT;
                        flush = true;
                    } else {
                        builder.append(currentToken);
                    }
                    break;
                }
                default: {
                    if ("\'".equals(currentToken)) {
                        state = State.SINGLE_QUOTE;
                    } else if ("\"".equals(currentToken)) {
                        state = State.DOUBLE_QUOTE;
                    } else if (" ".equals(currentToken)) {
                        if (flush || builder.length() > 0) {
                            arguments.add(builder.toString());
                            builder.setLength(0);
                        }
                    } else {
                        builder.append(currentToken);
                    }
                    flush = false;
                    break;
                }
            }
            previousToken = currentToken;
        }
        return arguments;
    }

}
