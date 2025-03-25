#!/bin/bash
if [ -f "/opt/crac-files/inventory.img" ]; then
    echo "Restoring from checkpoint..."
    exec java -XX:CRaCRestoreFrom=/opt/crac-files
else
    echo "Starting fresh..."
    exec java -XX:CRaCCheckpointTo=/opt/crac-files -jar /opt/app/app.jar
fi
