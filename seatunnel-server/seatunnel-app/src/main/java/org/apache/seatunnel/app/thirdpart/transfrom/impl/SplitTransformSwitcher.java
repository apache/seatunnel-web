package org.apache.seatunnel.app.thirdpart.transfrom.impl;

import com.google.auto.service.AutoService;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.shade.com.typesafe.config.Config;
import org.apache.seatunnel.shade.com.typesafe.config.ConfigValueFactory;
import org.whaleops.whaletunnel.web.app.domain.request.job.TableSchemaReq;
import org.whaleops.whaletunnel.web.app.domain.request.job.transform.Split;
import org.whaleops.whaletunnel.web.app.domain.request.job.transform.SplitTransformOptions;
import org.whaleops.whaletunnel.web.app.domain.request.job.transform.Transform;
import org.whaleops.whaletunnel.web.app.domain.request.job.transform.TransformOptions;
import org.whaleops.whaletunnel.web.app.thirdpart.transfrom.TransformConfigSwitcher;
import org.whaleops.whaletunnel.web.dynamicforms.FormStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

@AutoService(TransformConfigSwitcher.class)
public class SplitTransformSwitcher implements TransformConfigSwitcher {
    @Override
    public Transform getTransform() {
        return Transform.SPLIT;
    }

    @Override
    public FormStructure getFormStructure(OptionRule transformOptionRule) {
        return null;
    }

    @Override
    public Config mergeTransformConfig(
            Config transformConfig, TransformOptions transformOption, TableSchemaReq inputSchema) {

        SplitTransformOptions splitTransformOptions = (SplitTransformOptions) transformOption;

        checkArgument(
                splitTransformOptions.getSplits().size() > 0,
                "SplitTransformSwitcher splits must be greater than 0");

        List<Map<String, Object>> splitOPs = new ArrayList<>();

        for (Split split : splitTransformOptions.getSplits()) {
            Map<String, Object> splitOP = new HashMap<>();
            splitOP.put("separator", split.getSeparator());
            splitOP.put("split_field", split.getSourceFieldName());
            splitOP.put("output_fields", ConfigValueFactory.fromIterable(split.getOutputFields()));
            splitOPs.add(splitOP);
        }

        return transformConfig.withValue("splitOPs", ConfigValueFactory.fromIterable(splitOPs));
    }
}
