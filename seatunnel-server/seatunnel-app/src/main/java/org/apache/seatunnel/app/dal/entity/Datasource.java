package org.apache.seatunnel.app.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_st_datasource")
public class Datasource {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("datasource_name")
    private String datasourceName;

    @TableField("plugin_name")
    private String pluginName;

    @TableField("plugin_version")
    private String pluginVersion;

    @TableField("datasource_config")
    private String datasourceConfig;

    @TableField("description")
    private String description;

    @TableField("create_user_id")
    private Integer createUserId;

    @TableField("update_user_id")
    private Integer updateUserId;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
