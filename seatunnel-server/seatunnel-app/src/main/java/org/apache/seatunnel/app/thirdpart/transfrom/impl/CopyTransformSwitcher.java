package org.apache.seatunnel.app.thirdpart.transfrom.impl;

import com.google.auto.service.AutoService;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.app.domain.request.job.TableSchemaReq;
import org.apache.seatunnel.app.domain.request.job.transform.Copy;
import org.apache.seatunnel.app.domain.request.job.transform.CopyTransformOptions;
import org.apache.seatunnel.app.domain.request.job.transform.Transform;
import org.apache.seatunnel.app.domain.request.job.transform.TransformOptions;
import org.apache.seatunnel.app.dynamicforms.FormStructure;
import org.apache.seatunnel.app.thirdpart.transfrom.TransformConfigSwitcher;
import org.apache.seatunnel.shade.com.typesafe.config.Config;

import java.util.LinkedHashMap;

import static org.apache.seatunnel.app.thirdpart.transfrom.TransformConfigSwitcherUtils.getOrderedConfigForLinkedHashMap;

@AutoService(TransformConfigSwitcher.class)
public class CopyTransformSwitcher implements TransformConfigSwitcher {
    @Override
    public Transform getTransform() {
        return Transform.COPY;
    }

    @Override
    public FormStructure getFormStructure(OptionRule transformOptionRule) {
        return null;
    }

    @Override
    public Config mergeTransformConfig(
            Config transformConfig, TransformOptions transformOption, TableSchemaReq inputSchema) {

        CopyTransformOptions copyTransformOptions = (CopyTransformOptions) transformOption;

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        for (Copy copy : copyTransformOptions.getCopyList()) {
            fields.put(copy.getTargetFieldName(), copy.getSourceFieldName());
        }

        return transformConfig.withValue("fields", getOrderedConfigForLinkedHashMap(fields).root());
    }
}
