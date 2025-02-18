.PHONY: start

start:
	@echo "Starting server..."
	@lsof -ti :8080 | xargs kill -9 2>/dev/null || true
	while true; do \
		java server/Server.java & \
		fswatch -1 -E -i '\.java$$' -e '\.java~$$' server ; \
		echo "Change detected, restarting server..."; \
		lsof -ti :8080 | xargs kill -9 2>/dev/null || true; \
		sleep 1; \
	done
