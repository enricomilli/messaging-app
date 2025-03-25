# MESSAGING APP

## Overview
This project is a simple messaging application written in Java. It consists of two main parts:
* A server (in messagingapp/server) that accepts connections, coordinates messages, and handles user commands.
* A client (in messagingapp/client) that connects to the server, checks username availability, and allows users to send messages (both public and private).

The project also has a Makefile that streamlines building and running the server and client, as well as running tests.

## Prerequisites
* Java Development Kit (JDK) installed (Java 8 or higher recommended)
* make (for using the provided Makefile)
* (Optionally) the JUnit library in the lib/ folder for running tests

## Project Structure
```
messagingapp/
├── client/         – Client source files (Client.java, ClientConfig.java, FlagHandler.java, MessagePrinter.java, MessagesHandler.java)
├── server/         – Server source files (Server.java, Connection.java, Coordinator.java, MessagesCoordinator.java, UserListMap.java)
├── tests/          – (Optional) Test source files (e.g., FlagHandlerTest.java, ClientConfigTest.java, etc.)
├── lib/            – External libraries (classpath additions, e.g., JUnit)
└── Makefile        – Contains targets to build and run the server, client, and tests
```

## Building and Running with the Makefile
The Makefile provides a few targets for building, running, testing, and cleaning your project. Edit the file if you want to change client startup arguments.

### 1. Build and start the Server:
* To compile the server code and immediately launch the server, run:
  ```
  make server
  ```
  This target first calls build-server to compile all the .java files in messagingapp/server with the classpath including lib/*.
  The server will display its host address and port (default port is 8080) and start listening for incoming connections.

### 2. Build and start the Client:
* To compile the client code and run it, run:
  ```
  make client
  ```
  This target compiles the .java files in messagingapp/client. It then starts the client using a set of default command-line arguments (defined by the ARGS variable in the Makefile):
  ```
  ARGS = -p 8080 -t 127.0.0.1 -i enrico
  ```
  These flags correspond to:
  - `-p` or `--port`: Specifies the server port (default: 8080)
  - `-t` or `--target-ip`: Specifies the server IP (default: 127.0.0.1)
  - `-i` or `--id`: Specifies (or assigns) the client's username/id

### 3. Building and Running Tests:
* If you have tests (using JUnit in the tests directory), you can compile and run them with:
  ```
  make test
  ```
  This target compiles the test files (adding messagingapp/client, messagingapp/server, and lib for dependencies) and uses JUnitCore to run tests.

### 4. Clean-up Compiled Files:
* To remove .class files from the client, server, and tests directories, run:
  ```
  make clean
  ```

## Manual Compilation and Running (Without Makefile)
If you prefer to build and run the application manually, here are the steps:

### Building the Server Manually:
1. Change directory to messagingapp.
2. Compile the server files:
   ```
   javac -cp "lib/*" server/*.java
   ```
3. Run the server (make sure you include the lib folder in the classpath):
   ```
   java -cp "server:lib/*" Server
   ```

### Building the Client Manually:
1. Change directory to messagingapp.
2. Compile the client files:
   ```
   javac -cp "lib/*" client/*.java
   ```
3. Run the client using the desired flags. For example, to connect to localhost on port 8080 with username "enrico":
   ```
   java -cp "client:lib/*" Client -p 8080 -t 127.0.0.1 -i enrico
   ```

## Notes on Client Flags and Usage
* The Client class uses a FlagHandler to process command-line flags supplied when launching it. The available flags (and their aliases) are:
  - `target-ip` (alias: `t`): Specifies the server IP address to join.
  - `port` (alias: `p`): Specifies the server port.
  - `id` (alias: `i`): Specifies the desired username. If not provided, a random UUID is generated.
* Once connected, the client shows a list of commands (for example, `/leave`, `/list`, and `/message`) to interact in the chat.
* The client also handles a controlled exit when Ctrl+C is pressed.

## Additional Information
* The server assigns the first user who connects as the "coordinator". The coordinator is responsible for handling certain commands (e.g., private messaging and listing chat members) and for periodically checking connected users via a heartbeat.
* If the coordinator disconnects, the server will select a new coordinator from the remaining clients.
* The Makefile is set up with a "build-tests" target for compiling tests and a "test" target to run JUnit tests if you would like to verify functionality.

