Build seatunnel-web
./mvnw clean install -DskipTests

Update mysql database details in src/test/resources/application.yml and Run the seatunnel-web-it integration tests
./mvnw -T 1C -B verify -DskipUT=true -DskipIT=false -DSEATUNNEL_HOME=/some/path/apache-seatunnel-2.3.7 -DST_WEB_BASEDIR_PATH=seatunnel-web-dist/target/apache-seatunnel-web-1.0.0-SNAPSHOT/apache-seatunnel-web-1.0.0-SNAPSHOT
NOTE: Please remember to update the versions according to the latest supported versions.

If you're using a version of Java higher than Java 8 for running the tests, add the following VM options: 
-DitJvmArgs="--add-opens java.base/java.lang.invoke=ALL-UNNAMED".

While running integrations tests from IDE, ensure following VM options are set
SEATUNNEL_HOME=/some/path/apache-seatunnel-2.3.7
ST_WEB_BASEDIR_PATH=/some/path/seatunnel-web-dist/target/apache-seatunnel-web-1.0.0-SNAPSHOT/apache-seatunnel-web-1.0.0-SNAPSHOT
