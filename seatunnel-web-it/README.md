Build seatunnel-web
./mvnw clean install -DskipTests

Run seatunnel-web-it integration tests
./mvnw -T 1C -B verify -DskipUT=true -DskipIT=false -DSEATUNNEL_HOME=/some/path/apache-seatunnel-2.3.11 -DST_WEB_BASEDIR_PATH=seatunnel-web-dist/target/apache-seatunnel-web-1.0.3-SNAPSHOT/apache-seatunnel-web-1.0.3-SNAPSHOT
NOTE: Please remember to update the versions according to the latest supported versions.

If you're using a version of Java higher than Java 8 for running the tests, add the following VM options: 
-DitJvmArgs="-Xmx1024m --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED"

While running integrations tests from IDE, ensure following VM options are set
SEATUNNEL_HOME=/some/path/apache-seatunnel-2.3.11
ST_WEB_BASEDIR_PATH=/some/path/seatunnel-web-dist/target/apache-seatunnel-web-1.0.3-SNAPSHOT/apache-seatunnel-web-1.0.3-SNAPSHOT

By default, integration tests use the H2 database. If you want to use the MySQL database, update the MySQL database details in src/test/resources/application.yml and run the seatunnel-web-it integration tests with the -DdbType=mysql option as shown below:
./mvnw -T 1C -B verify -DskipUT=true -DskipIT=false -DdbType=mysql -DSEATUNNEL_HOME=/some/path/apache-seatunnel-2.3.11 -DST_WEB_BASEDIR_PATH=seatunnel-web-dist/target/apache-seatunnel-web-1.0.3-SNAPSHOT/apache-seatunnel-web-1.0.3-SNAPSHOT
