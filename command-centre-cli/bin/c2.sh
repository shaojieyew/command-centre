cd "$(dirname "$0")"
cd ..
export C2_HOME="${PWD}"
java -jar $C2_HOME/boot/command-centre-cli.jar $*