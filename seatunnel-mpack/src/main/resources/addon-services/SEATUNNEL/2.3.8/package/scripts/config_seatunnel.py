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
from resource_management.core.resources.system import File
from resource_management.core.source import InlineTemplate


def configure_seatunnel():
    import params
    File(params.seatunnel_yaml,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.seatunnel_content_template)
         )

    File(params.seatunnel_jvm_options,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.jvm_options_template)
         )

    File(params.seatunnel_jvm_client_options,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.jvm_client_options_template)
         )

    File(params.seatunnel_log4j2,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.log4j2_properties_template)
         )

    File(params.seatunnel_log4j2_client,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.log4j2_client_properties_template)
         )

    # Write content to hazelcast.yaml
    File(params.seatunnel_hazelcast,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.hazelcast_content_template)
         )

    # Write content to hazelcast-client.yaml
    File(params.seatunnel_hazelcast_client,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.hazelcast_client_content_template)
         )

    File(params.seatunnel_env_file,
         mode=0o644,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=InlineTemplate(params.seatunnel_env_template)
         )

    File(params.seatunnel_home_config_file,
         mode=0o755,
         owner=params.seatunnel_user,
         group=params.seatunnel_group,
         content=f'''export SEATUNNEL_HOME={params.seatunnel_home}
         export PATH=$PATH:$SEATUNNEL_HOME/bin'''
         )
