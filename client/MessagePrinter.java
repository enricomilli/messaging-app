/**
 * MessagePrinter handles printing incoming messages while
 * scrolling the user input so that it stays with the most
 * recent message
 * 
 */
class MessagePrinter {
    private final String userId;
    private final InputBuffer inputBuffer;
    private static final String CLEAR_LINE = "\033[2K";
    private static final String MOVE_TO_LINE_START = "\r";

    public MessagePrinter(String userId, InputBuffer inputBuffer) {
        this.userId = userId;
        this.inputBuffer = inputBuffer;
    }

    public synchronized void printMessage(String message) {
        // Save current input
        String currentInput = inputBuffer.getCurrentInput();

        // Clear the current line
        System.out.print(MOVE_TO_LINE_START + CLEAR_LINE);

        // Print the received message
        System.out.println(message);

        // Reprint the input prompt and current input
        System.out.print(userId + ": " + currentInput);
    }
}
