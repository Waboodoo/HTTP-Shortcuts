package ch.rmy.curlparser;

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
                        String[] header = iterator.next().split(": ", 0);
                        if (header.length == 2) {
                            command.headers.put(header[0], header[1]);
                        }
                        continue;
                    }
                    case "-d":
                    case "--data": {
                        command.data = iterator.next();
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
                }
            }

            // arguments with 0 parameters
            switch (argument) {
                case "-G":
                case "--get": {
                    command.method = "GET";
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
