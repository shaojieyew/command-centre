### Start spark-yarn cluster
Prerequisite: install Docker and make sure Docker daemon is running
1. `mvn compile`
or
2. `docker-compose -f C:/command-centre/spark-yarn-cluster/docker-compose.yml up -d`
or
3. `cd C:/command-centre/spark-yarn-cluster` & `docker-compose -f up`
### Stop cluster


1. `docker-compose -f C:/command-centre/spark-yarn-cluster/docker-compose.yml down`
2. `docker-compose down`

Note:
\
Using docker-compose down will not store any state.
\
Also, `docker-compose stop` doesn't stop yarn gracefully at the moment, so when its started again, there will be problem running jobs.

### Ports

Port | Remark 
--- | --- 
http://localhost:9001/ | Hadoop NameNode Web UI
http://localhost:8088/ | Yarn Web UI
http://localhost:8080/ | Spark Web UI
http://localhost:18080/ | SparkHistory Web UI
http://localhost:8081/ | Nifi
localhost:9000/ | Hadoop NameNode
localhost:50010/ | Hadoop DataNode Data Transfer Port
localhost:8032/ | Yarn resource manager
localhost:9092 | Kafka Service
### Spark-submit

1. Download and extract: https://archive.apache.org/dist/spark/spark-2.4.1/spark-2.4.1-bin-hadoop2.7.tgz
2. Set spark project 
\
`set SPARK_HOME=C:/spark-2.4.1-bin-hadoop2.7/bin`
3. Set hadoop conf project
\
`set HADOOP_CONF_DIR=C:/spark-yarn-cluster/config`
4. copy _command-centre/spark-yarn-cluster/spark/conf/spark-defaults.conf_  into _C:/spark-2.4.1-bin-hadoop2.7/conf_
5. `spark-submit --master yarn --deploy-mode cluster --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.1 --executor-memory 1G --total-executor-cores 1 --num-executors 1
    --files ../src/main/resources/config.yml --class example.data.app.SparkApp spark-app-1.0.0-SNAPSHOT.jar config.yml app1`

Note:
 - spark-shell can only be ran from within the container
 - `--packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.1` is required for spark to read from kafka
### Use container's terminal
1. Get container id using `docker ps` or `docker ps -aqf "name=spark-yarn-cluster"`
2. Enter terminal using `docker exec -it [containerid]`

### Start latest postgress
Prerequisite: install Docker and make sure Docker daemon is running
1. `cd C:/command-centre/postgress` & `docker-compose up -d`
### Stop postgress
1. `docker-compose down`

Note:

Using docker-compose down will not store any state.
Hence will need mount volumes
### Mount volumes
mounting of volume to to preserve data database-data:/var/lib/postgresql/data/ 

### Operation of Database
$ docker-compose run database bash 

# drop into the container shell
database# psql --host=database --username=<username> --dbname=<db_name>
refer to database.project for more info
