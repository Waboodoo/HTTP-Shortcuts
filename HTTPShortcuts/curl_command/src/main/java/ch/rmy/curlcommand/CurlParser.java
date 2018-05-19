package ch.rmy.curlcommand;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.ListIterator;

public class CurlParser {

    public static CurlCommand parse(String commandString) {
        List<String> arguments = CommandParser.parseCommand(commandString);
        CurlParser curlParser = new CurlParser(arguments);
        return curlParser.builder.build();
    }

    private final CurlCommand.Builder builder = new CurlCommand.Builder();

    private CurlParser(List<String> arguments) {
        ListIterator<String> iterator = arguments.listIterator();
        boolean urlFound = false;
        while (iterator.hasNext()) {
            int index = iterator.nextIndex();
            String argument = iterator.next();

            if (argument.equalsIgnoreCase("curl") && index == 0) {
                continue;
            }

            if (argument.startsWith("-") && !argument.startsWith("--") && argument.length() > 2) {
                String argumentParameter = argument.substring(2);
                argument = argument.substring(0, 2);
                iterator.add(argumentParameter);
                iterator.previous();
            }

            // arguments with 1 parameter
            if (iterator.hasNext()) {
                switch (argument) {
                    case "-X":
                    case "--request": {
                        builder.method(iterator.next());
                        continue;
                    }
                    case "-H":
                    case "--header": {
                        String[] header = iterator.next().split(": ", 2);
                        if (header.length == 2) {
                            builder.header(header[0], header[1]);
                        }
                        continue;
                    }
                    case "-d":
                    case "--data":
                    case "--data-binary":
                    case "--data-urlencode": {
                        String data = iterator.next();
                        if (argument.equals("--data-urlencode")) {
                            try {
                                if (data.contains("=")) {
                                    String[] parts = data.split("=", 2);
                                    data = parts[0] + "=" + URLEncoder.encode(parts[1], "utf-8");
                                } else {
                                    data = URLEncoder.encode(data, "utf-8");
                                }
                            } catch (UnsupportedEncodingException e) {
                            }
                        }
                        builder.data(data);
                        continue;
                    }
                    case "-m":
                    case "--max-time":
                    case "--connect-timeout": {
                        try {
                            builder.timeout(Integer.parseInt(iterator.next()));
                        } catch (NumberFormatException e) {
                        }
                        continue;
                    }
                    case "-u":
                    case "--user": {
                        String[] credentials = iterator.next().split(":", 2);
                        builder.username(credentials[0]);
                        if (credentials.length > 1) {
                            builder.password(credentials[1]);
                        }
                        continue;
                    }
                    case "-A":
                    case "--user-agent": {
                        builder.header("User-Agent", iterator.next());
                        continue;
                    }
                    case "--url": {
                        String url = iterator.next();
                        if (url.toLowerCase().startsWith("http")) {
                            builder.url(url);
                        } else {
                            builder.url("http://" + url);
                        }
                        continue;
                    }
                    case "-e":
                    case "--referer": {
                        builder.header("Referer", iterator.next());
                        continue;
                    }
                }
            }

            // arguments with 0 parameters
            switch (argument) {
                case "-G":
                case "--get": {
                    builder.method(CurlCommand.METHOD_GET);
                    continue;
                }
            }

            if (argument.toLowerCase().startsWith("http")) {
                urlFound = true;
                builder.url(argument);
            } else if (!argument.startsWith("-") && !urlFound) {
                urlFound = true;
                builder.url("http://" + argument);
            }
        }
    }

}
