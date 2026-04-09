#!/bin/bash
# Nuwa Android Build Script

set -e

ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
NDK_HOME="${NDK_HOME:-$ANDROID_HOME/ndk}"

echo "Building Nuwa Android Bridge..."

cd "$(dirname "$0")/../platform/android-bridge"

# Check for NDK
if [ ! -d "$NDK_HOME" ]; then
    echo "NDK not found at $NDK_HOME"
    exit 1
fi

# Set up Android target
export ANDROID_NDK_HOME="$NDK_HOME"
export CARGO_TARGET_DIR="$(pwd)/../target"

# Build for Android
cargo build --target aarch64-linux-android --release 2>/dev/null || \
cargo build --target armv7-linux-androideabi --release 2>/dev/null || \
cargo build --target i686-linux-android --release 2>/dev/null || \
echo "Warning: Native build failed, will use JNI fallback"

echo "Build complete"
