package org.apache.seatunnel.app.domain.dto.job;

import org.apache.seatunnel.app.dal.entity.JobInstance;

import lombok.Data;

/** @Description @ClassName SeaTunnelJobInstanceDto @Author zhang @Date 2023/7/4 11:19 */
@Data
public class SeaTunnelJobInstanceDto extends JobInstance {
    private String jobDefineName;

    private long readRowCount;

    private long writeRowCount;

    private Long runningTime;
}
