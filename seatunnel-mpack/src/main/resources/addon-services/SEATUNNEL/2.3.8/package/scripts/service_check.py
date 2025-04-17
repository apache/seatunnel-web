"""
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

"""
import socket

from resource_management import Fail
from resource_management.core.logger import Logger
from resource_management.libraries.functions.format import format
from resource_management.libraries.script.script import Script
from seatunnel import Seatunnel
from seatunnel_web import Seatunnel_web


class ServiceCheck(Script):
    def service_check(self, env):
        import params
        env.set_params(params)
        hostname = socket.gethostname()
        if hostname in params.seatunnel_hosts_list:
            Logger.info('Checking Seatunnel service')
            try:
                Seatunnel().status(env)
            except Exception as e:
                Logger.error(f'Seatunnel service check failed error: {e}')
                raise Fail(format("Seatunnel on host {hostname} is not running, error={e}"))

        if hostname in params.seatunnel_web_hosts_list:
            Logger.info('Checking Seatunnel-web status')
            try:
                Seatunnel_web().status(env)
            except Exception as e:
                Logger.error(f'Seatunnel-web service check failed error: {e}')
                raise Fail(format("Seatunnel-web on host {hostname} is not running, error={e}"))


if __name__ == "__main__":
    ServiceCheck().execute()
