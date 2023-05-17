package org.apache.seatunnel.app.domain.request.job;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.seatunnel.app.domain.request.connector.BusinessMode;

@Data
@ApiModel(value = "JobDefinition Request", description = "job info")
public class JobReq {
    @ApiModelProperty(value = "job name", required = true, dataType = "String")
    private String name;

    @ApiModelProperty(value = "job description", dataType = "String")
    private String description;

    @ApiModelProperty(value = "job type", dataType = "String")
    private BusinessMode jobType;

    @ApiModelProperty(value = "project code", dataType = "Long")
    private Long projectCode;
}
