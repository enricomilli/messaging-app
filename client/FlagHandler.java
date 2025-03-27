import java.util.HashMap;

class FlagHandler {

    private HashMap<String, String> argsFound = new HashMap<>();
    private HashMap<String, String> flagPairs = new HashMap<>();

    public FlagHandler(String[] args) {
        this.argsFound = parseFlags(args);
        this.flagPairs = createFlagPairs();
    }

    private HashMap<String, String> parseFlags(String[] args) {
        Integer totalArgs = args.length;
        HashMap<String, String> parsedArgs = new HashMap<>();

        for (int i = 0; i < totalArgs; i++) {

            String arg = args[i];

            if (arg.startsWith("--")) {
                String flagName = arg.substring(2); // removes the --

                if (flagName.contains("=")) {
                    String[] parts = flagName.split("=", 2);
                    parsedArgs.put(parts[0], parts[1]);

                } else if (i + 1 < totalArgs && !args[i + 1].startsWith("-")) { // Check for space
                    parsedArgs.put(flagName, args[i + 1]);
                    i++;

                } else { // For flags that don’t need a value
                    parsedArgs.put(flagName, "true");
                }

            } else if (arg.startsWith("-")) {
                String flagName = arg.substring(1);
                if (flagName.contains("=")) {
                    String[] parts = flagName.split("=", 2);
                    parsedArgs.put(parts[0], parts[1]);
                } else if (i + 1 < totalArgs && !args[i + 1].startsWith("-")) { // Check for space
                    parsedArgs.put(flagName, args[i + 1]);
                    i++;
                } else {
                    parsedArgs.put(flagName, "true");
                }
            }

        }

        return parsedArgs;
    }

    private HashMap<String, String> createFlagPairs() {
        HashMap<String, String> pairs = new HashMap<>();

        pairs.put("id", "i");
        pairs.put("i", "id");

        pairs.put("port", "p");
        pairs.put("p", "port");

        pairs.put("target-ip", "t");
        pairs.put("t", "target-ip");

        return pairs;
    }

    public Boolean hasFlag(String flag) {
        if (argsFound.containsKey(flag)) {
            return true;
        }
        String alias = flagPairs.get(flag);
        if (alias != null) {
            return argsFound.containsKey(alias);
        }
        return false;
    }

    /**
     * This function accepts the shorthand or longhand form of a flag and returns
     * the input value whether it was input as long or shorthand.
     *
     * @return The value the user input for the flag, or null if not found.
     */
    public String getValue(String flag) {
        // The arg passed in
        if (argsFound.containsKey(flag)) {
            return argsFound.get(flag);
        }

        // If not found, try its alias
        String alias = flagPairs.get(flag);
        if (alias != null && argsFound.containsKey(alias)) {
            return argsFound.get(alias);
        }
        return null;
    }

}
