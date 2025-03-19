
class InputBuffer {
    private volatile String currentInput = "";

    public synchronized void setCurrentInput(String input) {
        this.currentInput = input;
    }

    public synchronized String getCurrentInput() {
        return currentInput;
    }
}
