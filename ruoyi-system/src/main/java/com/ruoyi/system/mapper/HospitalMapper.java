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
     * @param semiMajorAxis1 内圈椭圆长轴
     * @param semiMinorAxis1 内圈椭圆短轴
     * @param semiMajorAxis2 外圈椭圆长轴
     * @param semiMinorAxis2 外圈椭圆短轴
     */
    List<Hospital> selectHospitAffectPoints(@Param("longitude") Double longitude,
                                            @Param("latitude") Double latitude,
                                            @Param("semiMajorAxis1") Double semiMajorAxis1,
                                            @Param("semiMinorAxis1") Double semiMinorAxis1,
                                            @Param("semiMajorAxis2") Double semiMajorAxis2,
                                            @Param("semiMinorAxis2") Double semiMinorAxis2);
}
