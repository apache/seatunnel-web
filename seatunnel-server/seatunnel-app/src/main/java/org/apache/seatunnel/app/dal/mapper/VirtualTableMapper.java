package org.apache.seatunnel.app.dal.mapper;

import org.apache.seatunnel.app.dal.entity.VirtualTable;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface VirtualTableMapper extends BaseMapper<VirtualTable> {

    IPage<VirtualTable> selectPage(IPage<VirtualTable> page);

    IPage<VirtualTable> selectVirtualTablePageByParam(
            IPage<VirtualTable> page,
            @Param("pluginName") String pluginName,
            @Param("datasourceName") String datasourceName);

    int checkVirtualTableNameUnique(
            @Param("tableId") Long tableId,
            @Param("virtualDatabaseName") String databaseName,
            @Param("virtualTableName") String virtualTableName);
}
