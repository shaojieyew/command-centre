#!/bin/bash

is_file_path=false
arg=()
for i  in "$@"
do
  if [ "$is_file_path" = true ]; then
    is_file_path=false
    arg=$(realpath "$i")
  else
    if [ "$i" == "-f" ]; then
      is_file_path=true
      arg=$i
    else
      if [ "$i" == "-rf" ]; then
        is_file_path=true
        arg=$i
      else
        arg=$i
      fi
    fi
  fi
done

cd "$(pwd)/$(dirname "$0")/.."
export C2_HOME=$(pwd)
java -jar $C2_HOME/boot/command-centre-cli.jar ${args[@]}