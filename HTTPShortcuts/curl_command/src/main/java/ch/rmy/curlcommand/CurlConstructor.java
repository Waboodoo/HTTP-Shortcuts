package ch.rmy.curlcommand;

import java.util.Map;

public class CurlConstructor {

    public static String toCurlCommandString(CurlCommand curlCommand) {
        CommandLineBuilder builder = new CommandLineBuilder("curl");

        builder.argument(curlCommand.getUrl());

        if (!"GET".equals(curlCommand.getMethod())) {
            builder.option("-X", curlCommand.getMethod());
        }

        if (curlCommand.getTimeout() != 0) {
            builder.option("-m", curlCommand.getTimeout());
        }

        if (!curlCommand.getUsername().isEmpty()) {
            builder.option("-u", curlCommand.getUsername() + (!curlCommand.getPassword().isEmpty() ? ":" + curlCommand.getPassword() : ""));
        }

        for (Map.Entry<String, String> header : curlCommand.getHeaders().entrySet()) {
            builder.option("-H", header.getKey() + ": " + header.getValue());
        }

        if (!curlCommand.getData().isEmpty()) {
            builder.option("-d", curlCommand.getData());
        }

        return builder.build();
    }

}
