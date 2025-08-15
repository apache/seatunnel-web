# Deploy

### 1 Preparing the Apache SeaTunnel environment

Because SeaTunnel Web uses the SeaTunnel Java Client to submit jobs, running SeaTunnel Web requires preparing a SeaTunnel Zeta Engine service first.

Based on the usage requirements of SeaTunnel Zeta Engine, the SeaTunnel Client node that submits the job must have the same operating system and installation directory structure as the SeaTunnel Server node that runs the job. Therefore, if you want to run SeaTunnel Web in IDEA, you must install and run a SeaTunnel Zeta Engine Server on the same machine as the IDEA.

Don't worry, the next steps will tell you how to correctly install SeaTunnel Zeta Engine Server in different situations.

### 2 Run SeaTunnel Web In Server
To run SeaTunnel Web on the server, you need to first have a SeaTunnel Zeta Engine Server environment. If you do not already have one, you can refer to the following steps for deployment.

#### 2.1 Deploy SeaTunnel Zeta Engine Server In Server Node

You have two ways to get the SeaTunnel installer package. Build from source code or download from the SeaTunnel website.

**The SeaTunnel version used here is only for writing this document to show you the process used, and does not necessarily represent the correct version. SeaTunnel Web and SeaTunnel Engine have strict version dependencies, and you can confirm the specific version mapping through xxx**

**Support SeaTunnel Version**

- SeaTunnel 2.3.3 Only

##### 2.1.1 Build from source code
* Get the source package from https://seatunnel.apache.org/download or https://github.com/apache/seatunnel.git
* Build installer package use maven command `./mvnw -U -T 1C clean install -DskipTests -D"maven.test.skip"=true -D"maven.javadoc.skip"=true -D"checkstyle.skip"=true -D"license.skipAddThirdParty" `
* Then you can get the installer package in `${Your_code_dir}/seatunnel-dist/target`, For example:`apache-seatunnel-2.3.3-SNAPSHOT-bin.tar.gz`

##### 2.1.2 Download installer package
The other way to get SeaTunnel Zeta Engine Server installer package is download the installer package from https://seatunnel.apache.org/download and install plugins online.

* Download and install connector plugin(Some third-party dependency packages will also be automatically downloaded and installed during this process, such as hadoop jar). You can get the step from https://seatunnel.apache.org/docs/2.3.3/start-v2/locally/deployment.
* After completing the previous step, you will receive an installation package that can be used to install SeaTunnel Zeta Engine Server on the server. Run `tar -zcvf apache-seatunnel-2.3.3-SNAPSHOT-bin.tar.gz apache-seatunnel-2.3.3-SNAPSHOT`

##### 2.1.3 Deploy SeaTunnel Zeta Server
After 3.1.1 or 3.1.2 you can get an installer package `apache-seatunnel-2.3.3-SNAPSHOT-bin.tar.gz`, Then you can copy it to you server node and deploy reference https://seatunnel.apache.org/docs/seatunnel-engine/deployment.

##### 2.1.4 Deploy SeaTunnel Zeta Client In SeaTunnel Web Run Node
If you use SeaTunnel Web, you need deploy a SeaTunnel Zeta Client in the SeaTunnel Web run Node. **If you run SeaTunnel Zeta Server and SeaTunnel Web in same node, you can skip this step**.

* Copy `apache-seatunnel-2.3.3-SNAPSHOT-bin.tar.gz` to the SeaTunnel Web node and unzip it **in the same path of SeaTunnel Zeta Server node**.
* Set `SEATUNNEL_HOME` to environment variable like SeaTunnel Zeta Server node.
* Config `hazelcast-client.yaml` reference https://seatunnel.apache.org/docs/seatunnel-engine/deployment#6-config-seatunnel-engine-client
* Run `$SEATUNNEL_HOME/bin/seatunnel.sh --config $SEATUNNEL_HOME/config/v2.batch.config.template`, If this job run finished, it indicates successful client deployment.

#### 2.2 Download and Install SeaTunnel Web

1. Download seatunnel web from https://seatunnel.apache.org/download
2. Copy the `apache-seatunnel-web-bin-${project.version}.tar.gz` to your server node and unzip it.

```shell
tar -zxvf apache-seatunnel-web-bin-${project.version}.tar.gz
```

#### 2.3 Init database

1. Edit `apache-seatunnel-web-bin-${project.version}/script/seatunnel_server_env.sh` file, Complete the installed database address, port, username, and password. Here is an example:

    ```
    export HOSTNAME="localhost"
    export PORT="3306"
    export USERNAME="root"
    export PASSWORD="123456"
    ```
2. Run init shell `sh apache-seatunnel-web-bin-${project.version}/script/init_sql.sh` If there are no errors during operation, it indicates successful initialization.

#### 2.4 Config application and Run SeaTunnel Web Backend Server

* Edit `apache-seatunnel-web-bin-${project.version}/conf/application.yml` Fill in the database connection information in the file.
* Copy `$SEATUNNEL_HOME/config/hazelcast-client.yaml` to `apache-seatunnel-web-bin-${project.version}/conf/`
* Copy `apache-seatunnel-2.3.3-SNAPSHOT/connectors/plugin-mapping.properties` file to `apache-seatunnel-web-bin-${project.version}/conf/` dir.

#### 2.5 Start SeaTunnel Web

```shell
cd apache-seatunnel-web-${project.version}
sh bin/seatunnel-backend-daemon.sh start
```

Accessing in a browser http://127.0.0.1:8801/ui/ Okay, the default username and password are admin/admin.
