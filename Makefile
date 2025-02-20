.PHONY: server build-client client clean

ARGS = -p 8080 -t 127.0.0.1 -i enrico

server:
	@echo "Starting server..."
	@lsof -ti :8080 | xargs kill -9 2>/dev/null || true
	while true; do \
		java server/Server.java & \
		fswatch -1 -E -i '\.java$$' -e '\.java~$$' server ; \
		echo "Change detected, restarting server..."; \
		lsof -ti :8080 | xargs kill -9 2>/dev/null || true; \
		sleep 1; \
	done


build-client:
	@cd client && javac Client.java FlagHandler.java

client: build-client
	@cd client && java Client $(ARGS)

clean:
	@rm -f client/*.class
