package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.system.entity.FireFighter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@DataSource(value = DataSourceType.SLAVE)
@Mapper
public interface FireFighterMapper extends BaseMapper<FireFighter> {

    /**
     * 查询震中附件救援队伍信息
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     */
    List<FireFighter> selectFireFighterPoints(@Param("longitude") Double longitude,
                                              @Param("latitude") Double latitude);
}
