#!/bin/bash
cd "$(pwd)/$(dirname "$0")/.."
export C2_HOME=$(pwd)
java -jar $C2_HOME/boot/command-centre-cli.jar $*