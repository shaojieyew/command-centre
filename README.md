⚠️This is a project made to improve our provisioning on big data tools such as NiFi, Kafka, Spark. This has been moved to an private repository and will not be maintained.

# Command Centre Modules

| Project     | Description |
| ----------- | ----------- |
| command-centre-ui      |  frontend app      |
| command-centre-api      |  command center api that builds around the command-centre-core |
| command-centre-cli      |  command line application      |
| command-centre-service   |   consolidated services; hadoop, yarn, spark, nifi, git, artifactory       |
| command-centre-database   |  docker project that builds a postgres docker image           |
| spark-app   | simple spark application for testing job submission to cluster        |
| yarn-cluster   | docker project that builds a single node cluster with yarn, hadoop, spark, nifi and kafka installed        |
| command-centre-example   |  command centre service examples      |

## Prequsite:
### install node.js, npm and yarn on windows 
Follow instruction on https://phoenixnap.com/kb/install-node-js-npm-on-windows
For yarn: https://classic.yarnpkg.com/en/docs/install#windows-stable

Once yarn is install, you will need to pull the following package by:
1) npm install --global next
2) npm install --global react
3) npm install --global react-dom

### Install docker on windows 
This is required for running docker image as container
https://hub.docker.com/editions/community/docker-ce-desktop-windows

## Running on Dev
#### Run Frontend App
http://localhost:3000
```
cd command-centre-ui
yarn dev

yarn start // for prod; using built files
```
#### Run Frontend App on Docker
http://localhost:3000
```
cd command-centre-ui
docker-compose up -d
```
#### Run Api
swagger url: http://localhost:7000/doc/api.html
```
cd command-centre-api
mvn spring-boot:run 
```
To connect API to postgres database, change active profile to DEV in application.properties. 
A password encryptor need to be provided. Also, see below on how to run postgres in docker.

```
mvn spring-boot:run  -Dspring-boot.run.jvmArguments="-Djasypt.encryptor.password=XXXXX"
```

#### Run Database on docker
```
cd command-centre-database

# start container
docker-compose -d up

# remove container
docker-compose down
```

To delete database data
```
docker volume ls
docker volume rm XXXXXX
```

#### Run Yarn Cluster on docker
```
cd yarn-cluster

# start container
docker-compose -d up

# remove container
docker-compose down
```
Address | Remark 
--- | --- 
http://localhost:9001/ | Hadoop NameNode Web UI
http://localhost:8088/ | Yarn Web UI
http://localhost:8080/ | Spark Web UI
http://localhost:18080/ | SparkHistory Web UI
#### Submit spark job
```
cd spark-app/target
spark-submit --master yarn --deploy-mode cluster --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.1 --executor-memory 1G --total-executor-cores 1 --num-executors 1 --files ../src/main/resources/config.yml --class app.StreamingSparkApp spark-app-1.0.1-SNAPSHOT.jar config.yml app1
```

# Why command centre
Command centre will provide a one stop platform for provisioning Spark jobs and other ETL workflows 
## Issues/pain points
##### Deployment process
1.	Stop running jobs
2.	Disable/avoid scheduled recovery scripts 
3.	Flush all the data in pipeline
4.	Backup & Delete checkpoint folder,as the records in Kafka and checkpoint folder may not be compatible with previously deployed jobs 
5.	Deploy new version of Jar and Config 

##### Points
1.	Manual effort during deployment
      -	Copy & paste new jar from artifactory/jenkins
      -	Remove and flush stored data (Kafka/Parquet/Watermark)
      -	Backup for rollback
      -	Disable/avoid scheduled recovery scripts
      -	Manual backup of Config file
2.	Recovery script is being maintained separately. Adding new jobs will need to change the recovery script
3.	Lack of config file management

## Minimum Requirements
1.	Start & stop Nifi processors
2.	Start a spark job with a specified jar from artifactory and specified config file from repo
3.	Stop a job
4.	View list of running jobs
5.	Move checkpoint folder to backup

# Setup IDE for Auto Java Code Formatting
Follow the following guide. Ignore the prettier part
https://github.com/yclim/gem/wiki/Setup-Auto-Code-Formatting-on-Intellij
