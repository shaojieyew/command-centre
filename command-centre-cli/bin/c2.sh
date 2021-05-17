cd "$(dirname "$0")"
export C2_HOME="${PWD}"
java -jar boot\command-centre-cli.jar $*