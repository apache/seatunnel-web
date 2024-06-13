# 1.0.1 Release Note

## Feature

### DataSource & Connector

- [DataSource & Connector] Add Mongodb datasource.
- [DataSource & Connector] Add connector db2.

## Improvement

- [Improve][UI] Translate Route Names
- [Improve] When starting, check whether the environment variable "SEATUNNEL_HOME" exists or not.
- [Improve] Build images using flexible version parameters.
- [Improve] Add "download_datasource.sh" for downloading datasource.

## Fix

- [Fix] Fix arbitrary file readvulnerability on mysql jdbc
- [Fix] Remove secretKey in application.yml
- [Fix] fix ts problems with mvn clean package
- [Fix] fix: update "driver-class-name: com.mysql.cj.jdbc.Driver" for mysql8
- [Fix] Fix job config file generation path error in windows
- [Fix] virtual update datasource
- [Fix] Fix dynamic form item show by other item bug
- [Fix] Fix release src problem
