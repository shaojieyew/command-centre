## Spark application and Data generator for data pipeline simulation

### Prerequisite
start up yarn cluster and kafka; see yarn-cluster readme
### Run data generator
Following method generates data and published into kafka
```
generator.Main.main(String arg[])
```

### Run spark app on yarn cluster


Spark streaming
```
mvn clean package
cd target
spark-submit --master yarn --deploy-mode cluster --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.1 --executor-memory 1G --total-executor-cores 1 --num-executors 1 --files ../src/main/resources/config.yml --class app.StreamingSparkApp spark-app-1.0.1-SNAPSHOT.jar config.yml app1
```  

Running spark app without kafka
```
spark-submit --master yarn --deploy-mode cluster --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.1 --executor-memory 1G --total-executor-cores 1 --num-executors 1 --files ../src/main/resources/config.yml --class app.SparkApp spark-app-1.0.1-SNAPSHOT.jar config.yml app1
```  
