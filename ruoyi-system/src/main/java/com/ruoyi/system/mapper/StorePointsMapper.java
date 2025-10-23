package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.system.entity.StorePoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DataSource(value = DataSourceType.SLAVE)
@Mapper
public interface StorePointsMapper extends BaseMapper<StorePoints> {

    /**
     * 查询震中附件救援物资信息
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     */
    List<StorePoints> selectStorePoints(@Param("longitude") Double longitude,
                                        @Param("latitude") Double latitude);
}
