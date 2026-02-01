#!/bin/bash

# Build and Deploy Script for Advanced DOCX Reporter JMeter Plugin
# This script builds the plugin and copies it to the JMeter lib/ext directory

set -e

# Configuration
JMETER_HOME="${JMETER_HOME:-$HOME/Documents/01Project/jmeter/apache-jmeter-5.6.3}"
PLUGIN_DIR="$(cd "$(dirname "$0")" && pwd)"
TARGET_DIR="$PLUGIN_DIR/target"
PLUGIN_JAR="advanced-docx-reporter-1.0.0.jar"

echo "============================================"
echo "Advanced DOCX Reporter - Build & Deploy"
echo "============================================"
echo ""

# Check if JMeter exists
if [ ! -d "$JMETER_HOME" ]; then
    echo "ERROR: JMeter not found at $JMETER_HOME"
    echo "Please set JMETER_HOME environment variable or update this script"
    exit 1
fi

echo "JMeter Home: $JMETER_HOME"
echo "Plugin Directory: $PLUGIN_DIR"
echo ""

# Build the plugin
echo "Building plugin..."
cd "$PLUGIN_DIR"
mvn clean package -DskipTests

if [ ! -f "$TARGET_DIR/$PLUGIN_JAR" ]; then
    echo "ERROR: Build failed - JAR file not found"
    exit 1
fi

echo ""
echo "Build successful!"
echo ""

# Deploy to JMeter
echo "Deploying to JMeter..."
JMETER_EXT_DIR="$JMETER_HOME/lib/ext"

if [ ! -d "$JMETER_EXT_DIR" ]; then
    echo "ERROR: JMeter lib/ext directory not found: $JMETER_EXT_DIR"
    exit 1
fi

# Remove old version if exists
if [ -f "$JMETER_EXT_DIR/$PLUGIN_JAR" ]; then
    echo "Removing old version..."
    rm -f "$JMETER_EXT_DIR/$PLUGIN_JAR"
fi

# Copy new version
cp "$TARGET_DIR/$PLUGIN_JAR" "$JMETER_EXT_DIR/"

echo ""
echo "============================================"
echo "Deployment complete!"
echo "============================================"
echo ""
echo "Plugin installed to: $JMETER_EXT_DIR/$PLUGIN_JAR"
echo ""
echo "To use the plugin:"
echo "1. Start JMeter: $JMETER_HOME/bin/jmeter"
echo "2. Add a Listener: Right-click on Test Plan > Add > Listener > Advanced DOCX Reporter"
echo ""
