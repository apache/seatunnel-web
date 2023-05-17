package org.apache.seatunnel.app.thirdpart.transfrom.impl;

import com.google.auto.service.AutoService;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.whaleops.whaletunnel.web.app.domain.request.job.TableSchemaReq;
import org.whaleops.whaletunnel.web.app.domain.request.job.transform.Transform;
import org.whaleops.whaletunnel.web.app.domain.request.job.transform.TransformOptions;
import org.whaleops.whaletunnel.web.app.thirdpart.transfrom.TransformConfigSwitcher;
import org.whaleops.whaletunnel.web.dynamicforms.FormStructure;

@AutoService(TransformConfigSwitcher.class)
public class FiledEventTypeTransformSwitcher implements TransformConfigSwitcher {
    @Override
    public Transform getTransform() {
        return Transform.FILTERROWKIND;
    }

    @Override
    public FormStructure getFormStructure(OptionRule transformOptionRule) {
        return null;
    }

    @Override
    public Config mergeTransformConfig(
            Config transformConfig, TransformOptions transformOption, TableSchemaReq inputSchema) {
        return transformConfig;
    }
}
