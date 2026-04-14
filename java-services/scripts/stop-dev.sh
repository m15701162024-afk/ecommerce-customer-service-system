#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=========================================="
echo "   Stopping Dev Mode Services             "
echo "=========================================="

cd "$PROJECT_DIR/logs"

for pid_file in *.pid; do
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        service=$(basename "$pid_file" .pid)
        echo "Stopping $service (PID: $pid)..."
        kill $pid 2>/dev/null || echo "$service already stopped"
        rm -f "$pid_file"
    fi
done

echo "=========================================="
echo "   All services stopped                   "
echo "==========================================