@echo off
setlocal

rem
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem    http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem

set "WORKDIR=%~dp0"

set "VERSION=latest"
if not "%2"=="" (
    set "VERSION=%2"
)

if /i "%1"=="code" (
    goto :code
)
if /i "%1"=="image" (
    goto :image
)

goto :usage

:code
call "%WORKDIR%mvnw.cmd" clean package -DskipTests -Pci
for /f "delims=" %%F in ('dir /b /s "%WORKDIR%seatunnel-web-dist\target\apache-seatunnel-web-*.zip"') do (
    move "%%F" "%WORKDIR%"
)
goto :eof

:image
docker buildx build --load --no-cache -t apache/seatunnel-web:%VERSION% -t apache/seatunnel-web:latest -f "%WORKDIR%docker\backend.dockerfile" .
goto :eof

:usage
echo "Usage: build.bat {code|image}"
exit /b 1

:eof
endlocal
