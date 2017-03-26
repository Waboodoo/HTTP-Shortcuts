package ch.rmy.curlparser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.ListIterator;

public class CurlParser {

    public static CurlCommand parse(String commandString) {
        List<String> arguments = CommandParser.parseCommand(commandString);
        CurlParser curlParser = new CurlParser(arguments);
        return curlParser.command;
    }

    private final CurlCommand command = new CurlCommand();

    private CurlParser(List<String> arguments) {
        ListIterator<String> iterator = arguments.listIterator();

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
                        command.method = iterator.next();
                        continue;
                    }
                    case "-H":
                    case "--header": {
                        String[] header = iterator.next().split(": ", 2);
                        if (header.length == 2) {
                            command.headers.put(header[0], header[1]);
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
                        if (!command.data.isEmpty()) {
                            data = command.data + data;
                        }
                        command.data = data;
                        if (CurlCommand.METHOD_GET.equals(command.method)) {
                            command.method = CurlCommand.METHOD_POST;
                        }
                        continue;
                    }
                    case "-m":
                    case "--max-time":
                    case "--connect-timeout": {
                        try {
                            command.timeout = Integer.parseInt(iterator.next());
                        } catch (NumberFormatException e) {

                        }
                        continue;
                    }
                    case "-u":
                    case "--user": {
                        String[] credentials = iterator.next().split(":", 2);
                        command.username = credentials[0];
                        if (credentials.length > 1) {
                            command.password = credentials[1];
                        }
                        continue;
                    }
                    case "-A":
                    case "--user-agent <name>": {
                        command.headers.put("User-Agent", iterator.next());
                        continue;
                    }
                    case "--url": {
                        String url = iterator.next();
                        if (url.toLowerCase().startsWith("http")) {
                            command.url = url;
                        } else {
                            command.url = "http://" + url;
                        }
                        continue;
                    }
                    case "-e":
                    case "--referer": {
                        command.headers.put("Referer", iterator.next());
                        continue;
                    }
                }
            }

            // arguments with 0 parameters
            switch (argument) {
                case "-G":
                case "--get": {
                    command.method = CurlCommand.METHOD_GET;
                    continue;
                }
            }

            if (argument.toLowerCase().startsWith("http")) {
                command.url = argument;
            } else if (!argument.startsWith("-") && command.url.isEmpty()) {
                command.url = "http://" + argument;
            }
        }
    }

}
