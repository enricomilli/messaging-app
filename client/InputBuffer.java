/**
 * InputBuffer handles storing the user input
 * while they have not sent out the message (input state)
 *
 */
class InputBuffer {
    private volatile String currentInput = "";

    public synchronized void setCurrentInput(String input) {
        this.currentInput = input;
    }

    public synchronized String getCurrentInput() {
        return currentInput;
    }
}
