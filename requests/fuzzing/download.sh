#!/usr/bin/env sh

cd "$(dirname "$0")" || exit

VERSION=3.3.0
JAR=evomaster.jar
URL=https://github.com/EMResearch/EvoMaster/releases/download/v$VERSION/$JAR

wget $URL
