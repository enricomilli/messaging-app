.PHONY: server build-server client build-client test build-tests clean

# Command-line arguments for client startup
ARGS = -p 8080 -t 127.0.0.1 -i enrico

# Adding the classpath for the downloaded code
CP_LIB = lib/*

build-server:
	@javac -cp "$(CP_LIB)" server/*.java
	@echo "Server built"

server: build-server
	@java -cp "server:$(CP_LIB)" Server

build-client:
	@javac -cp "$(CP_LIB)" client/*.java

client: build-client
	@java -cp "client:$(CP_LIB)" Client $(ARGS)

build-tests:
	@javac -cp "$(CP_LIB):client:server" tests/*.java
	@echo "Tests built"

test: build-tests
	@java -cp "tests:client:server:$(CP_LIB)" org.junit.runner.JUnitCore FlagHandlerTest ClientConfigTest UserListMapTest MessagesCoordinatorTest

clean:
	@rm -f client/*.class server/*.class tests/*.class

