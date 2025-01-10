package org.apache.seatunnel.app.domain;

import com.hazelcast.nonapi.io.github.classgraph.json.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskInfo {
    @Id private Long id;

    private String taskId;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 处理结果
    private String result;
}
