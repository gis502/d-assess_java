package com.ruoyi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.system.domain.Hospital;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DataSource(value = DataSourceType.SLAVE)
@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {

    /**
     * 查询椭圆范围内风险源
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     */
    List<Hospital> selectHospitAffectPoints(@Param("longitude") Double longitude,
                                            @Param("latitude") Double latitude);
}
