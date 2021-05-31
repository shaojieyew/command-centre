## Command center CLI
A tool to manage spark application and nifi processors using declarative templates

### Functionalities
- [List Spark Application](#list-spark-application)
- [Start Spark Application](#start-spark-application)  
- [Stop Spark Application](#stop-spark-application) 
- [List Nifi Processors](#list-nifi-processors)
- [Start Nifi Processors](#start-nifi-processors)
- [Stop Nifi Processors](#stop-nifi-processors)
- [List Spark checkpoints and Kafka offsets](#list-spark-checkpoints-and-kafka-offsets)
- [Get Spark checkpoints offsets](#get-spark-checkpoints-offsets)
- [Backup Spark checkpoint](#backup-spark-checkpoint)
- [List checkpoint backup](#list-checkpoint-backup)
- [Restore Spark checkpoint](#restore-spark-checkpoint)

## Setup

1. `mvn clean package`
2. extract command-centre-cli/target/command-centre-cli-0.0.1-SNAPSHOT.zip to desire location
3. add the extracted command-centre-cli-0.0.1-SNAPSHOT/bin directory to OS environment path

### Configuration

By default, the application will read setting.yml from default path `<application directory>/config/setting.yml`

To explicit specify setting use `-c D:/setting.yml` or `--config D:/config.yml`  

include ` -Djava.security.krb5.conf=D:/krb5.conf` in c2.bat or c2.sh as JVM argument when using kerberos
```
# tmpDirectory is use by application to store temporary files
tmpDirectory: "D:/tmp"

# sparkHome is required for spark-submit 
sparkHome:  "D:/spark-2.4.1-bin-hadoop2.7"

# maven repository setting
mavenProperties:
  - url: "https://gitlab.com/api/v4/projects/111111/packages/maven"
    privateToken: "SAd6FTHRVdfgertGTFW325G"
    type: "gitlab" # jfrog / gitlab - optional field, witout it, wouldm eans lesson functionalities

  - url: "https://gitlab.com/api/v4/projects/2222/packages/maven"
    privateToken: "SAd6FTHRVdfgertGTFW325G"

# nifi setting
nifiProperties:
  host: "http://localhost:8081"
  kerberos: 
    keytab: "C:/tmp/user.keytab"
    principle: "User@domain.com"

# git repositiories for resource reference
gitProperties:
  - url: "https://gitlab.com/111/command-centre.git"
    token: "SAd6FTHRVdfgertGTFW325G"

  - url: "https://github.com/222/command-centre.git"
    token: "SADdfhge213Dw"

# yarn cluster for running spark application
hadoopYarnProperties:
  coreSite : C:/Users/core-site.xml
  hdfsSite : C:/Users/hdfs-site.xml
  yarnSite : C:/Users/yarn-site.xml
  webHdfsHost: "http://localhost:9001"
  yarnHost: "http://localhost:8088"
  username: "User"
  kerberos: 
    keytab: "C:/tmp/user.keytab"
    principle: "User@domain.com"

# spark checkpoint settings
sparkCheckpointProperties:
  # kafka setting reading kafka offset
  kafkaProperties:
    kafkaHosts:
      - "http://localhost:9092"
    kerberos: 
      keytab: "C:/tmp/user.keytab"
      principle: "User@domain.com"

  # webhdfs setting for managing checkpoint directory
  webHdfsProperties:
    webHdfsHost: "http://localhost:9001"
    username: "User"
    kerberos: 
      keytab: "C:/tmp/user.keytab"
      principle: "User@domain.com"
  # directory in webhdfs for managing checkpoint backups
  backupDir: "/user/user/checkpointBackup"

```
## Spec files
Spec files are declarative templates that defines the specification of instruction for managing Spark application and Nifi Processors. It is required to run most of the actions.

Spec files can be included in a command by either specifying the file `-f C:/example/sparkEtl.yml` or directory `-f C:/example/sparkEtl`
use `-rf` for recursive
Only files that have the correct Spec file format will be included. Example below.

### SparkDeployment 
This spec file define the attributes require to start the spark applications.
```
kind : SparkDeployment

# the parent fields outside of spec field will be inherited by the spark deployment spec
artifact : c2.spark-app.1.0.1-SNAPSHOT
sparkArgs :
 - name : spark.driver.memory
   value : 2G
artifact : c2.spark-app.1.0.1-SNAPSHOT
mainClass : app.SparkApp
jarArgs :
  - config.yml
sparkArgs :
  - name : spark.driver.memory
    value : 1G
    ...
resources:
  - name: config.yml
    source: https://gitlab.com/111/command-centre.git/-/refs/heads/master/-/spark-app/src/main/resources/config.yml
    type: git
  - name: abc.keytab
    ....

spec:
  - name : spark-app1
    artifact : c2.spark-app.1.0.1-SNAPSHOT
    mainClass : app.SparkApp
    jarArgs :
      - config.yml
      - app1
    sparkArgs :
      - name : spark.driver.memory
        value : 1G
      - name : spark.executor.instances
        value: 1
      - name : spark.executor.memory
        value : 1G
      - name : spark.executor.cores
        value : 1
    resources:
      # example of reference file from git
      - name: config.yml
        source: https://gitlab.com/111/command-centre.git/-/refs/heads/master/-/spark-app/src/main/resources/config.yml
        type: git

      # example of uploading file from local
      - name: abc.keytab
        source: C:/Desktop/abc.keytab
        type: local

      # example of passing in string content as file
      - name: blacklist.yml
        source: >
            {
                "blacklist": [
                    {
                    "field": "id"
                    "value": "1234"
                    },
                    {
                    "field": "name"
                    "value": "asd"
                    }
                ]
            }
        type: string

  - name: spark-app2 
  ...
  - name: spark-app3 
  ...
```
### NifiQuery
This spec file defines query on Nifi processors. Actions will be applied to the queried processors/process groups
```
kind : NifiQuery
spec:
  - name: query1
    id: 7329430b-0179-1000-542f-778071798361
    type: ProcessGroup
    scope: root # this is only applicable when performing start/stop action on type ProcessGroup. 
                # when root is specify, only processors that do not have any inflow will be updated

  - name: query2
    query: ^NiFi.*/aaa
    type: ProcessGroup

  - name: query3
    query: "/"
    type: GenerateFlowFile

```
## Examples
### List Spark Application
Only listed application name in the spec files will be display
```
c2.sh spark ls -f D:\command-centre\command-centre-cli\example

# recursive file list
c2.sh spark ls -rf D:\command-centre\command-centre-cli\example

# specifying the name of application would only show details of the spark application
c2.sh spark ls --name spark-etl

```
### Start Spark Application
```
c2.sh spark run -f D:\command-centre\command-centre-cli\example

# only spec of spark application with the exact name specified will be ran; case sensitive 
c2.sh spark run -f D:\command-centre\command-centre-cli\example --name spark-app
```
### Stop Spark Application
Only listed application name in the spec files will be killed
```
c2.sh spark stop -f D:\command-centre\command-centre-cli\example

# only spark application with the exact name specified will be killed; case sensitive 
c2.sh spark stop --name spark-app
```
### List Nifi Processors
```
c2.sh nifi ls -f D:\command-centre\command-centre-cli\example

# instead of using spec files, query or Nifi Component id can be specified inline 
c2.sh nifi ls --query "NiFi.*/aa" --process-type GenerateFlowFiles

# using Nifi component id
c2.sh nifi ls --id 27281353-0179-1000-a523-953b88dae040
```
### Start Nifi Processors
```
c2.sh nifi run -f D:\command-centre\command-centre-cli\example

# instead of using spec files, query or Nifi Component id can be specified inline 
c2.sh nifi run --query "NiFi.*/aa" --process-type GenerateFlowFiles

# using Nifi component id
c2.sh nifi run --id 27281353-0179-1000-a523-953b88dae040

# by having --root-processor, only processor at the start of nifiFlow in the ProcessGroup will be action-ed on
c2.sh nifi run --query "NiFi.*" --process-type ProcessGroup --root-processor
```
### Stop Nifi Processors
```
c2.sh nifi stop -f D:\command-centre\command-centre-cli\example

# instead of using spec files, query or Nifi Component id can be specified inline 
c2.sh nifi stop --query "NiFi.*/aa" --process-type GenerateFlowFiles

# using Nifi component id
c2.sh nifi stop --id 27281353-0179-1000-a523-953b88dae040

# by having --root-processor, only processor at the start of nifiFlow in the ProcessGroup will be action-ed on
c2.sh nifi stop --query "NiFi.*" --process-type ProcessGroup --root-processor
```
### List Spark checkpoints and Kafka offsets
this will list all the checkpoints directory and their offsets within the query directory path
```
# the directory or parent directory path of the checkpoint needs to be specified
c2.sh checkpoint ls -q "\user\all_checkpoints" 

# by having --show-backlog, only offsets of topic/partitions that have backlog will be listed
c2.sh checkpoint ls -q "\user\all_checkpoints" --show-backlog
```

### Get Spark checkpoints offsets
this print out the checkpoint file in the queried directory
```
# the directory or parent directory path of the checkpoint needs to be specified
c2.sh checkpoint get -q "\user\all_checkpoints" 

```
### Backup Spark checkpoint
!!! note that, this method will move the *all* the checkpoints in the directory to a backup directory; specified in setting.yml. It does not create a copy
```
# the directory or parent directory path of the checkpoint needs to be specified

c2.sh checkpoint mv --backup -q "\user\all_checkpoints" 
```
### List checkpoint backup
```
c2.sh checkpoint ls --backup 
```
### Restore Spark checkpoint
!!! note that, this method will restore the checkpoints to its original location. It does not create a copy
```
c2.sh checkpoint restore --backup --id 3034194245
```

 

