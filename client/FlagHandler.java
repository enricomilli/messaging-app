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
        HashMap<String, String> parsedArgs = new HashMap<String, String>();

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

                } else { // For flags that dont need a value
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
        HashMap<String, String> pairs = new HashMap<String, String>();

        pairs.put("id", "i");
        pairs.put("i", "id");

        pairs.put("port", "p");
        pairs.put("p", "port");

        pairs.put("target-ip", "t");
        pairs.put("t", "target-ip");

        return pairs;
    }

    public Boolean hasFlag(String flag) {
        String option1 = flag;
        if (argsFound.containsKey(option1) == true) {
            return true;
        }

        String option2 = flagPairs.get(flag);
        if (option2 != null) {
            return argsFound.containsKey(option2);
        }

        return false;
    }

    /**
     * This function accepts the shorthand or longhand from of a flag and returns
     * the inputed value whether it was inputed as long or shorthand
     * 
     * @return The value the user inputed for the flag
     */
    public String getValue(String flag) {

        // option1 try to get the arg with the value passed in
        String option1 = argsFound.get(flag);
        if (option1 != null) {
            return argsFound.get(option1);
        }

        // option2 get the other pair of the flag incase it was inputed that way
        String option2 = flagPairs.get(flag);
        if (option2 != null) {
            return argsFound.get(option2);
        }

        return null;
    }

    public static void main(String[] args) {
        // testing
        String[] testArgs = { "--verbose", "--output-file=report.txt", "-d", "debug_level", "--no-color", "--count",
                "10", "-v" };
        FlagHandler handler = new FlagHandler(testArgs);

        System.out.println("Has flag 'verbose': " + handler.hasFlag("verbose"));
        System.out.println("Value of 'output-file': " + handler.getValue("output-file"));
        System.out.println("Has flag 'd': " + handler.hasFlag("d"));
        System.out.println("Value of 'd': " + handler.getValue("d"));
        System.out.println("Has flag 'color': " + handler.hasFlag("color")); // Testing --no-color
        System.out.println("Value of 'color': " + handler.getValue("color")); // Testing --no-color
        System.out.println("Has flag 'count': " + handler.hasFlag("count"));
        System.out.println("Value of 'count': " + handler.getValue("count"));
        System.out.println("Has flag 'v': " + handler.hasFlag("v"));
        System.out.println("Value of 'v': " + handler.getValue("v"));

    }
}
