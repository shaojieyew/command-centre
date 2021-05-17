@echo off
cd /d %~dp0/..
set C2_HOME=%CD%
java -jar boot\command-centre-cli.jar %*