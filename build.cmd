@echo off
setlocal enabledelayedexpansion

REM Licensed to the Apache Software Foundation (ASF) under one or more
REM contributor license agreements.  See the NOTICE file distributed with
REM this work for additional information regarding copyright ownership.
REM The ASF licenses this file to You under the Apache License, Version 2.0
REM (the "License"); you may not use this file except in compliance with
REM the License.  You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.

REM Set working directory
SET WORKDIR=%~dp0

REM Set version number
IF "%2"=="" (
    SET VERSION=latest
) ELSE (
    SET VERSION=%2
)

IF "%1"=="code" (
    CALL :code
    GOTO :EOF
) ELSE IF "%1"=="image" (
    CALL :image
    GOTO :EOF
) ELSE (
    ECHO Usage: build.cmd {code^|image}
    EXIT /B 1
)

:code
ECHO Building code...
IF NOT EXIST "%WORKDIR%mvnw.cmd" (
    ECHO Error: mvnw.cmd not found in %WORKDIR%
    EXIT /B 1
)
CALL "%WORKDIR%mvnw.cmd" clean package -DskipTests -Pci
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Maven build failed with error code %ERRORLEVEL%
    EXIT /B %ERRORLEVEL%
)

REM Move release zip file
SET "ZIP_FOUND=false"
FOR /F "delims=" %%i IN ('dir /b "%WORKDIR%seatunnel-web-dist\target\apache-seatunnel-web-*.zip" 2^>NUL') DO (
    ECHO Moving %%i to %WORKDIR%
    MOVE "%WORKDIR%seatunnel-web-dist\target\%%i" "%WORKDIR%"
    SET "ZIP_FOUND=true"
)

IF "%ZIP_FOUND%"=="false" (
    ECHO Warning: No apache-seatunnel-web-*.zip file found in %WORKDIR%seatunnel-web-dist\target\
)

EXIT /B 0

:image
ECHO Building Docker image...
WHERE docker >NUL 2>NUL
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Docker is not installed or not in PATH
    EXIT /B 1
)

docker buildx build --load --no-cache -t apache/seatunnel-web:%VERSION% -t apache/seatunnel-web:latest -f "%WORKDIR%docker\backend.dockerfile" .
IF %ERRORLEVEL% NEQ 0 (
    ECHO Error: Docker build failed with error code %ERRORLEVEL%
    EXIT /B %ERRORLEVEL%
)
EXIT /B 0

:EOF
endlocal
EXIT /B 0 