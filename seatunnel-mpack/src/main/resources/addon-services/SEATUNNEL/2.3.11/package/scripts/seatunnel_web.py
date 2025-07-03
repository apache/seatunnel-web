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
import subprocess

from config_seatunnel_web import configure_seatunnel_web
from resource_management.core import shell
from resource_management.core.exceptions import ComponentIsNotRunning, Fail
from resource_management.core.resources.system import Execute
from resource_management.libraries.functions.format import format
from resource_management.libraries.script.script import Script
from setup_ranger_seatunnel import setup_ranger_seatunnel

class Seatunnel_web(Script):
    def install(self, env):
        import params
        env.set_params(params)
        self.install_packages(env)


    def configure(self, env, upgrade_type=None):
        import params
        env.set_params(params)
        configure_seatunnel_web()

    def start(self, env, upgrade_type=None):
        import params
        env.set_params(params)
        configure_seatunnel_web()
        setup_ranger_seatunnel(upgrade_type=upgrade_type)

        start_cmd = format('/usr/sbin/seatunnel-web start')
        Execute(start_cmd, user=params.seatunnel_user)

    def stop(self, env, upgrade_type=None):
        import params
        env.set_params(params)
        stop_cmd = format('/usr/sbin/seatunnel-web stop')
        Execute(stop_cmd, user=params.seatunnel_user)

    def status(self, env):
        import params
        env.set_params(params)
        status_cmd = format('/usr/sbin/seatunnel-web status')
        code, output, error = shell.call(status_cmd, user=params.seatunnel_user, stderr=subprocess.PIPE, logoutput=True)
        if code != 0:
            raise Fail(format("Failed to execute command {status_cmd}, error={error}"))

        if output and "is not running" in output:
            raise ComponentIsNotRunning("Seatunnel-web is not running")


if __name__ == "__main__":
    Seatunnel_web().execute()
