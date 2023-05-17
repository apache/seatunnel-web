package org.apache.seatunnel.app.thirdpart.transfrom;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.app.domain.request.job.TableSchemaReq;
import org.apache.seatunnel.app.domain.request.job.transform.Transform;
import org.apache.seatunnel.app.domain.request.job.transform.TransformOptions;
import org.apache.seatunnel.app.dynamicforms.FormStructure;
import org.apache.seatunnel.shade.com.typesafe.config.Config;

public interface TransformConfigSwitcher {

    Transform getTransform();

    FormStructure getFormStructure(OptionRule transformOptionRule);

    Config mergeTransformConfig(
            Config transformConfig, TransformOptions transformOption, TableSchemaReq inputSchema);
}
