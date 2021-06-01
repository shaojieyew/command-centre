#!/bin/bash
currentDir=$(pwd)
cd "$(pwd)/$(dirname "$0")/.."
export C2_HOME=$(pwd)
cd $currentDir
java -jar $C2_HOME/boot/command-centre-cli.jar $*