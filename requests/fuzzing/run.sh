#!/usr/bin/env sh

set -e

cd "$(dirname "$0")" || exit

if [ ! -f ./evomaster.jar ]; then
   ./download.sh
fi

rm -rf build

CONFIG_FILE=${1}

if [ -z "$CONFIG_FILE" ]; then
    echo "Usage: $0 <config_file>"
    exit 1
fi

java \
 --add-opens java.base/java.net=ALL-UNNAMED \
 --add-opens java.base/java.util=ALL-UNNAMED \
 -jar evomaster.jar \
 --configPath "$CONFIG_FILE"
