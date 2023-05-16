package org.apache.seatunnel.app.dal.dao.impl;

import org.apache.seatunnel.app.dal.dao.IDatasourceDao;
import org.apache.seatunnel.app.dal.entity.Datasource;
import org.apache.seatunnel.app.dal.mapper.DatasourceMapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import javax.annotation.Resource;

import java.util.List;

@Repository
public class DatasourceDaoImpl implements IDatasourceDao {

    @Resource
    private DatasourceMapper datasourceMapper;

    @Override
    public boolean insertDatasource(Datasource datasource) {
        return datasourceMapper.insert(datasource) > 0;
    }

    @Override
    public Datasource selectDatasourceById(Long id) {
        return datasourceMapper.selectById(id);
    }

    @Override
    public boolean deleteDatasourceById(Long id) {
        return datasourceMapper.deleteById(id) > 0;
    }

    @Override
    public Datasource queryDatasourceByName(String name) {
        return datasourceMapper.selectOne(
                new QueryWrapper<Datasource>().eq("datasource_name", name));
    }

    @Override
    public boolean updateDatasourceById(Datasource datasource) {
        return datasourceMapper.updateById(datasource) > 0;
    }

    @Override
    public boolean checkDatasourceNameUnique(String dataSourceName, Long dataSourceId) {
        return datasourceMapper.checkDataSourceNameUnique(dataSourceName, dataSourceId) <= 0;
    }

    @Override
    public IPage<Datasource> selectDatasourcePage(Page<Datasource> page) {
        return datasourceMapper.selectPage(page, new QueryWrapper<Datasource>());
    }

    @Override
    public IPage<Datasource> selectDatasourceByParam(
            Page<Datasource> page,
            List<Long> availableDatasourceIds,
            String searchVal,
            String pluginName) {

        QueryWrapper<Datasource> datasourceQueryWrapper = new QueryWrapper<>();
        datasourceQueryWrapper.in("id", availableDatasourceIds);
        if (searchVal != null
                && !searchVal.isEmpty()
                && pluginName != null
                && !pluginName.isEmpty()) {
            return datasourceMapper.selectPage(
                    page,
                    datasourceQueryWrapper
                            .eq("plugin_name", pluginName)
                            .like("datasource_name", searchVal));
        }
        if (searchVal != null && !searchVal.isEmpty()) {
            return datasourceMapper.selectPage(
                    page, datasourceQueryWrapper.like("datasource_name", searchVal));
        }
        if (pluginName != null && !pluginName.isEmpty()) {
            return datasourceMapper.selectPage(
                    page, datasourceQueryWrapper.eq("plugin_name", pluginName));
        }
        return datasourceMapper.selectPage(page, datasourceQueryWrapper);
    }

    @Override
    public String queryDatasourceNameById(Long id) {
        return datasourceMapper.selectById(id).getDatasourceName();
    }

    @Override
    public List<Datasource> selectDatasourceByPluginName(String pluginName, String pluginVersion) {
        return datasourceMapper.selectList(
                new QueryWrapper<Datasource>()
                        .eq("plugin_name", pluginName)
                        .eq("plugin_version", pluginVersion));
    }

    @Override
    public List<Datasource> selectDatasourceByIds(List<Long> ids) {
        return datasourceMapper.selectBatchIds(ids);
    }

    @Override
    public List<Datasource> queryAll() {
        return datasourceMapper.selectList(new QueryWrapper<>());
    }

    @Override
    public List<Datasource> selectByIds(List<Long> ids) {
        return datasourceMapper.selectBatchIds(ids);
    }

    @Override
    public List<Datasource> selectDatasourceByUserId(int userId) {
        return datasourceMapper.selectByUserId(userId);
    }
}
