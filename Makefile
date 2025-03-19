.PHONY: server build-client client clean

ARGS = -p 8080 -t 127.0.0.1 -i enrico

build-server:
	@javac server/*.java
	@echo "Server built"

server: build-server
	@java -cp server Server

build-client:
	@javac client/*.java
	@echo "Client built"

client: build-client
	@java -cp client Client $(ARGS)

clean:
	@rm -f client/*.class && rm -f server/*.class
